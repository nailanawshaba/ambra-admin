/*
 * Copyright (c) 2006-2013 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.AdminRolesService;
import org.ambraproject.admin.views.RolePermissionView;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserRole;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ambraproject.service.cache.Cache;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.type.StandardBasicTypes;
import org.plos.ned_client.ApiException;
import org.plos.ned_client.api.IndividualsApi;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Methods to Administer user roles
 */
public class AdminRolesServiceImpl extends HibernateServiceImpl implements AdminRolesService {

  private static final Logger log = LoggerFactory.getLogger(AdminRolesService.class);

  private static final String ROLES_LOCK = "PermissionsCache-Lock-";
  private Cache rolesCache;

  public void setNedService(NedServiceImpl nedService) {
    this.nedService = nedService;
  }

  private NedServiceImpl nedService;

  /**
   * Get all the roles associated with a user
   *
   * @param userProfileID
   *
   * @return
   */
  public Set<UserRoleView> getUserRoles(final Long userProfileID)
  {
    List<Object[]> userProfileRoles = (List<Object[]>) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        return session.createSQLQuery("select userProfileRoleJoinTable.userRoleID, userRole.roleName" +
            " from userProfileRoleJoinTable left join userRole on userRole.userRoleID=userProfileRoleJoinTable.userRoleID" +
            " where userProfileRoleJoinTable.userProfileID = " + userProfileID)
            .addScalar("userProfileRoleJoinTable.userRoleID", StandardBasicTypes.LONG)
            .addScalar("userRole.roleName", StandardBasicTypes.STRING).list();
      }
    });

    Set<UserRoleView> results = new HashSet<UserRoleView>();

    for (Object[] ur: userProfileRoles) {
      Long userRoleID = (Long) ur[0];
      String roleName = (String) ur[1];
      results.add(new UserRoleView(userRoleID, roleName, true));
    }

    return results;
  }

  /**
   * Get all the available user roles.  If the passed in user has the mentioned role, it will be
   * noted as such in the view
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<UserRoleView> getAllRoles(final Long userProfileID)
  {
    return (List<UserRoleView>)hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        List<Object[]> userProfileRoles = (List<Object[]>) hibernateTemplate.execute(new HibernateCallback() {
          @Override
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
            return session.createSQLQuery("select userProfileRoleJoinTable.userRoleID, userRole.roleName" +
                " from userProfileRoleJoinTable left join userRole on userRole.userRoleID=userProfileRoleJoinTable.userRoleID" +
                " where userProfileRoleJoinTable.userProfileID = " + userProfileID)
                .addScalar("userProfileRoleJoinTable.userRoleID", StandardBasicTypes.LONG)
                .addScalar("userRole.roleName", StandardBasicTypes.STRING).list();
          }
        });


        List<Object[]> results = (List<Object[]>)session.createCriteria(UserRole.class)
          .setProjection(Projections.projectionList()
            .add(Projections.property("ID"))
            .add(Projections.property("roleName"))).list();

        List<UserRoleView> roleViews = new ArrayList<UserRoleView>();

        for(Object[] row : results) {
          boolean assigned = false;

          for(Object[] ur: userProfileRoles) {
            Long userRoleID = (Long) ur[0];
            if(userRoleID.equals((Long) row[0])) {
              assigned = true;
              break;
            }
          }

          roleViews.add(new UserRoleView((Long)row[0], (String)row[1], assigned));
        }

        return roleViews;
      }
    });
  }

  /**
   * Revoke all the roles from the passed in userProfileID
   *
   * @param userProfileID
   */
  @SuppressWarnings("unchecked")
  public void revokeAllRoles(final Long userProfileID)
  {
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        session.createSQLQuery("delete from userProfileRoleJoinTable where " +
            "userProfileID = " + userProfileID).executeUpdate();

        return null;
      }
    });

    this.clearCache();
  }

  /**
   * Grant the passed in role to the passed in user
   *
   * @param userProfileID
   * @param roleId
   */
  @SuppressWarnings("unchecked")
  public void grantRole(final Long userProfileID, final Long roleId)
  {
    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        session.createSQLQuery("INSERT INTO userProfileRoleJoinTable (userRoleID, userProfileID) VALUES (" +
            roleId + ", " + userProfileID + ")").executeUpdate();

        return null;
      }
    });

    this.clearCache();
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public Long createRole(final String roleName)
  {
    return (Long)hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        UserRole userRole = new UserRole(roleName, null);

        //Add the role to the collection
        session.save(userRole);

        return userRole.getID();
      }
    });
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public void deleteRole(final Long roleId) {
    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
      UserRole ur = (UserRole)session.load(UserRole.class, roleId);

      /*
       If there is a way to do this with hibernate, I am all ears, I played with it a bit
       but didn't want it to be a timesink
      */
      session.createSQLQuery("delete from userProfileRoleJoinTable where " +
        "userRoleID = " + roleId).executeUpdate();

      session.delete(ur);

      return null;
      }
    });

    this.clearCache();
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public List<RolePermissionView> getRolePermissions(final Long roleId)
  {
    Set<UserRole.Permission> permissions = (Set<UserRole.Permission>)hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        UserRole ur = (UserRole)session.load(UserRole.class, roleId);

        return ur.getPermissions();
      }
    });

    List<RolePermissionView> results = new ArrayList<RolePermissionView>();

    for(UserRole.Permission p : UserRole.Permission.values()) {
      if(permissions.contains(p)) {
        results.add(new RolePermissionView(p.toString(), true));
      } else {
        results.add(new RolePermissionView(p.toString(), false));
      }
    }

    return results;
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public void setRolePermissions(final Long roleId, final String[] permissions)
  {
    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Set<UserRole.Permission> newPerms = new HashSet<UserRole.Permission>(permissions.length);

        for(String p : permissions) {
          newPerms.add(UserRole.Permission.valueOf(p));
        }

        UserRole ur = (UserRole)session.load(UserRole.class, roleId);
        ur.setPermissions(newPerms);
        session.save(ur);

        return null;
      }
    });

    this.clearCache();
  }

  /**
   * Does the user associated with the current security principle have the given permission?
   * @param permission The permission to check for
   * @param authId The authorization ID for the logged in user.
   *
   * @throws SecurityException if the user doesn't have the permission
   */
  public void checkPermission(final UserRole.Permission permission, final String authId) throws SecurityException {
    if (authId == null || authId.trim().length() == 0) {
      throw new SecurityException("There is no current user.");
    }

    Set<UserRole.Permission> perms = getPermissions(authId);

    if(perms == null) {
      throw new SecurityException("Current user does not have the defined permission of " + permission.toString());
    }

    for(UserRole.Permission p : perms) {
      if(p.equals(permission)) {
        return;
      }
    }

    throw new SecurityException("Current user does not have the defined permission of " + permission.toString());
  }

  public Set<UserRole.Permission> getPermissions(final String authId) {
    final Object lock = (ROLES_LOCK + authId).intern(); //lock @ Article level

    return rolesCache.get(authId,
        new Cache.SynchronizedLookup<Set<UserRole.Permission>, SecurityException>(lock) {
          public Set<UserRole.Permission> lookup() throws SecurityException {

            final Long userProfileID = getUserIdFromAuthId(authId);

            List<Object> userPermissions = (List<Object>) hibernateTemplate.execute(new HibernateCallback() {
              @Override
              public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createSQLQuery("select distinct userRolePermission.permission" +
                    " from userProfileRoleJoinTable" +
                    " left join userRolePermission on userRolePermission.userRoleID=userProfileRoleJoinTable.userRoleID" +
                    " where userRolePermission.permission is not NULL" +
                    " and userProfileRoleJoinTable.userProfileID = " + userProfileID).list();
              }
            });

            Set<UserRole.Permission> permissions = new HashSet<UserRole.Permission>();
            for (Object p: userPermissions) {
              try {
                permissions.add(UserRole.Permission.valueOf((String) p));
              } catch (IllegalArgumentException e) {
                log.info("ignoring permission string " + ((String) p) + " not mapped to UserRole.Permission");
              }
            }
            return permissions;
          }
        });
  }

  private Long getUserIdFromAuthId(final String authId) throws SecurityException {
    try {
      //"individuals/CAS/{authId}" =>
      IndividualsApi individualsApi = nedService.getIndividualsApi();
      IndividualComposite individualComposite = individualsApi.readIndividualByCasId(authId);
      List<Individualprofile> ipList = individualComposite.getIndividualprofiles();

      if (ipList.size() > 0) {
        Individualprofile ip = ipList.get(0);
        if (ip.getNedid() != null && ip.getNedid().intValue() != 0) {
          return ip.getNedid().longValue();
        }
      }
    } catch (ApiException e) {
      throw new SecurityException("valid NedId not found in result for authId=" + authId, e);
    }

    throw new SecurityException("user record not found for authId=" + authId);
  }

  public void checkLogin(String authId) throws SecurityException {
    if (authId != null) {
      return;
    }

    throw new SecurityException("Current user is not logged in");
  }

  /**
   * @param rolesCache The roles cache to use
   */
  @Required
  public void setRolesCache(Cache rolesCache) {
    this.rolesCache = rolesCache;
  }

  /**
   * @inheritDoc
   */
  public void clearCache()
  {
    this.rolesCache.removeAll();
  }
}
