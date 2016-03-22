##Ambra
[Ambra](http://www.ambraproject.org/) is an innovative Open Source platform for publishing [Open Access](http://www.plos.org/open-access/) research articles. It provides features for post-publication discussion that allows for a "living" document around which further scientific discoveries can be made. The platform is in active development by [PLOS (Public Library of Science)](http://www.plos.org/).

## Ambra Admin
Ambra Admin is the admin portal for publishing content, managing users, and managing user comments.

## Architecture
* Ambra is written in [Java](http://www.java.com). It uses [Spring](http://www.springsource.org/), [Struts](http://struts.apache.org/), and the [FreeMarker templating system](http://www.freemarker.org/) to construct HTML which is served by [Tomcat](http://tomcat.apache.org/). It uses the [jQuery JavaScript library](http://jquery.com/) to create an advanced user interface.
* Ambra uses [Hibernate](http://www.hibernate.org/) for the storage and retrieval of Java objects to the MySQL relational database.
* The [MogileFS](http://danga.com/mogilefs/) distributed filesystem is used to store digital objects. MogileFS allows for automatic file replication, a non-SAN RAID setup and no single point of failure. MogileFS can be easily swapped for the base Linux file system or [Amazon S3](http://aws.amazon.com/s3/).
* [Apache Solr](http://lucene.apache.org/solr/) is used as the search platform and uses the [Apache Lucene](http://lucene.apache.org/core/) Java search library for its core search engine.
* Ambra uses [CAS (Central Authentication Service)](http://www.jasig.org/cas) single sign-on service for user login and registration.

## More Documentation
In the Ambra project website at [http://www.ambraproject.org/trac](http://www.ambraproject.org/trac).

## Mailing List
Please direct developer questions to the [Ambra developers mailing list](http://www.ambraproject.org/mailman/listinfo/ambra-dev).

Please direct general user questions and discussions to the [Ambra users mailing list](http://www.ambraproject.org/mailman/listinfo/ambra-users).

## License
Ambra is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html). See [LICENSE](https://github.com/PLOS/ambra-admin/blob/master/LICENSE.md) for details.

## JIRA ticket:  PS-1144 (To integrate Ambra-Admin with NED-API.)

TODO:

1. Refactor isDisplayNameInuse() and isEmailInUse() to call NED-API.

2. Refactor findUsersViaNed() to iterate over a list of IndividualComposite objects.
   Currently, this method retrieves the first IndividualComposite object, for exact matching of
   an user-input parameter of auth id, email address, or display name.

3. The following files have been deleted:
   org.ambraproject.user.action.AdminUserProfileAction.java
   org.ambraproject.user.action.AdminUserAlertsAction.java
   src/main/webapp/templates/editUserAlerts.ftl
   src/main/webapp/templates/editUserProfile.ftl

3. Add unit test cases for calling NED-API?  The following test files, for interacting with Ambra-DB via Hibernate,
   have been removed:
   src/test/java/org/ambraproject/search/service/SearchUserServiceTest.java
   src/test/java/org/ambraproject/user/action/AdminUserProfileActionTest.java
   src/test/java/org/ambraproject/user/action/SearchUserActionTest.java
   src/test/java/org/ambraproject/user/action/AdminUserAlertsActionTest.java

4. /src/main/java/org/ambraproject/admin/service/impl/NedServiceImpl.java

5. Improve logging and exception handling in the SearchUserServiceImpl methods (i.e., findUsersViaNed(), etc.)?

6. In pom.xml, use the <ned-java-client.version> tag for the ned-java-client dependency.

7. Javadoc