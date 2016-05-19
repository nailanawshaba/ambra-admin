package org.ambraproject.admin.service;

import com.sun.jersey.api.client.GenericType;
import org.ambraproject.action.BaseTest;
import org.ambraproject.admin.service.impl.NedServiceImpl;
import org.plos.ned_client.ApiClient;
import org.plos.ned_client.ApiException;
import org.plos.ned_client.api.IndividualsApi;
import org.plos.ned_client.model.Address;
import org.plos.ned_client.model.Alert;
import org.plos.ned_client.model.Auth;
import org.plos.ned_client.model.Degree;
import org.plos.ned_client.model.Email;
import org.plos.ned_client.model.Group;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.plos.ned_client.model.Phonenumber;
import org.plos.ned_client.model.Relationship;
import org.plos.ned_client.model.Uniqueidentifier;
import org.testng.annotations.AfterClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DummyNedServiceImpl extends NedServiceImpl {
  private IndividualsApi individualsApi;

  public static class DummyIndividualsApi extends IndividualsApi {

    List<IndividualComposite> individuals;
    IndividualComposite adminComposite, editorialComposite, userComposite;

    public DummyIndividualsApi() {


      individuals = new ArrayList<IndividualComposite>();

      Individualprofile adminProfile = new Individualprofile();
      adminProfile.setId(BaseTest.USER_PROFILE_ID_ADMIN.intValue());
      adminProfile.setNedid(BaseTest.USER_PROFILE_ID_ADMIN.intValue());
      adminProfile.setSource("Ambra");
      adminProfile.setBiography("");
      adminProfile.setDisplayname("Admin");
      adminProfile.setFirstname("Admin");
      adminProfile.setLastname("User");
      adminProfile.setIsactive(true);
      Email adminEmail = new Email();
      adminEmail.setId(BaseTest.USER_PROFILE_ID_ADMIN.intValue());
      adminEmail.setNedid(BaseTest.USER_PROFILE_ID_ADMIN.intValue());
      adminEmail.setSource("Ambra");
      adminEmail.setEmailaddress("admin@test.org");
      adminEmail.setType("Work");
      adminEmail.setVerified(true);
      adminEmail.setIsactive(true);
      Auth adminAuth = new Auth();
      adminAuth.setId(BaseTest.USER_PROFILE_ID_ADMIN.intValue());
      adminAuth.setNedid(BaseTest.USER_PROFILE_ID_ADMIN.intValue());
      adminAuth.setSource("Ambra");
      adminAuth.setAuthid(BaseTest.DEFAULT_ADMIN_AUTHID);
      adminAuth.setEmail("admin@test.org");
      adminAuth.setVerified(true);
      adminAuth.setIsactive(true);

      adminComposite = new IndividualComposite();
      adminComposite.getIndividualprofiles().add(adminProfile);
      adminComposite.getAuth().add(adminAuth);
      adminComposite.getEmails().add(adminEmail);
      individuals.add(adminComposite);

      Individualprofile editorialProfile = new Individualprofile();
      editorialProfile.setId(BaseTest.USER_PROFILE_ID_EDITORIAL.intValue());
      editorialProfile.setNedid(BaseTest.USER_PROFILE_ID_EDITORIAL.intValue());
      editorialProfile.setSource("Ambra");
      editorialProfile.setBiography("");
      editorialProfile.setDisplayname("Editorial");
      editorialProfile.setFirstname("Editorial");
      editorialProfile.setLastname("User");
      editorialProfile.setIsactive(true);
      Email editorialEmail = new Email();
      editorialEmail.setId(BaseTest.USER_PROFILE_ID_EDITORIAL.intValue());
      editorialEmail.setNedid(BaseTest.USER_PROFILE_ID_EDITORIAL.intValue());
      editorialEmail.setSource("Ambra");
      editorialEmail.setEmailaddress("editorial@test.org");
      editorialEmail.setType("Work");
      editorialEmail.setVerified(true);
      editorialEmail.setIsactive(true);
      Auth editorialAuth = new Auth();
      editorialAuth.setId(BaseTest.USER_PROFILE_ID_EDITORIAL.intValue());
      editorialAuth.setNedid(BaseTest.USER_PROFILE_ID_EDITORIAL.intValue());
      editorialAuth.setSource("Ambra");
      editorialAuth.setAuthid(BaseTest.DEFAULT_EDITORIAL_AUTHID);
      editorialAuth.setEmail("editorial@test.org");
      editorialAuth.setVerified(true);
      editorialAuth.setIsactive(true);

      editorialComposite = new IndividualComposite();
      editorialComposite.getIndividualprofiles().add(editorialProfile);
      editorialComposite.getAuth().add(editorialAuth);
      editorialComposite.getEmails().add(editorialEmail);
      individuals.add(editorialComposite);

      Individualprofile userProfile = new Individualprofile();
      userProfile.setId(BaseTest.USER_PROFILE_ID_NONADMIN.intValue());
      userProfile.setNedid(BaseTest.USER_PROFILE_ID_NONADMIN.intValue());
      userProfile.setSource("Ambra");
      userProfile.setBiography("");
      userProfile.setDisplayname("NonAdmin");
      userProfile.setFirstname("NonAdmin");
      userProfile.setLastname("User");
      userProfile.setIsactive(true);
      Email userEmail = new Email();
      userEmail.setId(BaseTest.USER_PROFILE_ID_NONADMIN.intValue());
      userEmail.setNedid(BaseTest.USER_PROFILE_ID_NONADMIN.intValue());
      userEmail.setSource("Ambra");
      userEmail.setEmailaddress("nonAdmin@test.org");
      userEmail.setType("Work");
      userEmail.setVerified(true);
      userEmail.setIsactive(true);
      Auth userAuth = new Auth();
      userAuth.setId(BaseTest.USER_PROFILE_ID_NONADMIN.intValue());
      userAuth.setNedid(BaseTest.USER_PROFILE_ID_NONADMIN.intValue());
      userAuth.setSource("Ambra");
      userAuth.setAuthid(BaseTest.DEFAULT_USER_AUTHID);
      userAuth.setEmail("nonAdmin@test.org");
      userAuth.setVerified(true);
      userAuth.setIsactive(true);

      userComposite = new IndividualComposite();
      userComposite.getIndividualprofiles().add(userProfile);
      userComposite.getAuth().add(userAuth);
      userComposite.getEmails().add(userEmail);
      individuals.add(userComposite);
    }

    @Override
    public Individualprofile addProfile(Integer nedId, Individualprofile body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void checkPassword(Integer nedId, Auth body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Address createAddress(Integer nedId, Address body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Alert createAlert(Integer nedId, Alert body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Email createEmail(Integer nedId, Email body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Group createGroup(Integer nedId, Group body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public IndividualComposite createIndividual(IndividualComposite body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Relationship createRelationship(Integer nedId, Relationship body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Uniqueidentifier createUid(Integer nedId, Uniqueidentifier body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Address> deleteAddress(Integer nedId, Integer addressId, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void deleteAlert(Integer nedId, Integer alertId, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void deleteEmail(Integer nedId, Integer emailId, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void deleteGroup(Integer nedId, Integer groupId, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public IndividualComposite deleteIndividual(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void deleteProfile(Integer nedId, Integer profileId, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void deleteRelationship(Integer nedId, Integer relationshipId, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public void deleteUid(Integer nedId, Integer id, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<IndividualComposite> findIndividuals(String entity, String attribute, String value) throws ApiException {
      List<IndividualComposite> result = new ArrayList<IndividualComposite>();

      if ("auth".equals(entity) && "authid".equals(attribute)) {
        if (BaseTest.DEFAULT_ADMIN_AUTHID.equals(value)) {
          result.add(adminComposite);
        } else if (BaseTest.DEFAULT_EDITORIAL_AUTHID.equals(value)) {
          result.add(editorialComposite);
        } else if (BaseTest.DEFAULT_USER_AUTHID.equals(value)) {
          result.add(userComposite);
        }
      } else if ("email".equals(entity) && "emailaddress".equals(attribute)) {
        if ("admin@test.org".equals(value)) {
          result.add(adminComposite);
        } else if ("editorial@test.org".equals(value)) {
          result.add(editorialComposite);
        } else if ("nonAdmin@test.org".equals(value)) {
          result.add(userComposite);
        }
      } else if ("individualprofile".equals(entity) && "displayname".equals(attribute)) {
        if ("Admin".equals(value)) {
          result.add(adminComposite);
        } else if ("Editorial".equals(value)) {
          result.add(editorialComposite);
        } else if ("NonAdmin".equals(value)) {
          result.add(userComposite);
        }
      }
      throw new ApiException(400, "Invalid param, configure in DummyNedServiceImpl");
    }

    @Override
    public Address getAddress(Integer nedId, Integer addressId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Address> getAddresses(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Alert getAlert(Integer nedId, Integer alertId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Alert> getAlerts(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Auth> getAuthRecord(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Degree> getDegrees(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Email getEmail(Integer nedId, Integer emailId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Email> getEmails(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Group getGroup(Integer nedId, Integer groupId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Group> getGroups(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Phonenumber> getPhonenumbers(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Individualprofile getProfile(Integer nedId, Integer profileId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Individualprofile> getProfiles(Integer nedId) throws ApiException {
      if (BaseTest.USER_PROFILE_ID_ADMIN.equals(nedId)) {
        return adminComposite.getIndividualprofiles();
      } else if (BaseTest.USER_PROFILE_ID_EDITORIAL.equals(nedId)) {
        return editorialComposite.getIndividualprofiles();
      } else if (BaseTest.USER_PROFILE_ID_NONADMIN.equals(nedId)) {
        return userComposite.getIndividualprofiles();
      }
      throw new ApiException(400, "Invalid nedId, configure in DummyNedServiceImpl");

    }

    @Override
    public Relationship getRelationship(Integer nedId, Integer relationshipId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Relationship> getRelationships(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Uniqueidentifier getUid(Integer nedId, Integer id) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public List<Uniqueidentifier> getUids(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public IndividualComposite readIndividual(Integer nedId) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public IndividualComposite readIndividualByCasId(String casId) throws ApiException {
      if (BaseTest.DEFAULT_ADMIN_AUTHID.equals(casId)) {
        return adminComposite;
      } else if (BaseTest.DEFAULT_EDITORIAL_AUTHID.equals(casId)) {
        return editorialComposite;
      } else if (BaseTest.DEFAULT_USER_AUTHID.equals(casId)) {
        return userComposite;
      }
      throw new ApiException(400, "Invalid casId, configure in DummyNedServiceImpl");
    }

    @Override
    public IndividualComposite readIndividualByUid(String uidType, String uidValue) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Address updateAddress(Integer nedId, Integer addressId, Address body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Alert updateAlert(Integer nedId, Integer alertId, String authorization, Alert body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Auth updateAuthRecord(Integer nedId, Integer authId, String authorization, Auth body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Email updateEmail(Integer nedId, Integer emailId, Email body, String authorization) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Group updateGroup(Integer nedId, Integer groupId, String authorization, Group body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Individualprofile updateProfile(Integer nedId, Integer profileId, String authorization, Individualprofile body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Relationship updateRelationship(Integer nedId, Integer relationshipId, String authorization, Relationship body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

    @Override
    public Uniqueidentifier updateUid(Integer nedId, Integer id, String authorization, Uniqueidentifier body) throws ApiException {
      throw new ApiException(501, "Not Implemented");
    }

  };

  /*
  [
  {
    "credentials": [
      {
      }
    ],
    "groups": [],
  }
]
   */

  public DummyNedServiceImpl() {

    individualsApi = new DummyIndividualsApi();
  }

  public IndividualsApi getIndividualsApi() {
    return individualsApi;
  }
}