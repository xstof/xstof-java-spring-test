# Authentication and Authorization with Spring and Azure Active Directory

## AAD Pure Authentication (logging in with OIDC) - sample: "AAD3Auth"

> **Remarks**
>
> - Method annotations don't seem to work (for example `@PreAuthorize("permitAll()")` on `AuthenticatedPage`)
> - In the `configure` method, one cannot seem to configure ".anyRequest()" as this is already done by `super.configure(http)`.  In that case need to override everything?
> - AAD roles do not automatically translate into an Authority (does not seem [to be implemented here](https://github.com/Azure/azure-sdk-for-java/blob/c92f8071a051b9b6212529ac86955c5e972b9f16/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADOAuth2UserService.java#L54))
> - Is calling of `super.configure(http)` required?
> - Are groups supposed to be picked up and validated against?  Does not seem to work with the `id_token` from  pure OIDC login.  ([This line](https://github.com/Azure/azure-sdk-for-java/blob/c92f8071a051b9b6212529ac86955c5e972b9f16/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADOAuth2UserService.java#L67) seems to suggest the logic only looks at the access token.)

Just authenticating with AAD does not require any authorization clients to be configured.  A configuration similar to the below is enough:

~~~yml
azure:
  activedirectory:
    tenant-id: 3fd11e85-d8ce-4c7f-b6a0-816346615777
    client-id: 35cea567-1674-40d4-95a4-e0bf5314f89f
    client-secret: xxx
~~~

When authcode is returned, redirect happens to: `/login/oauth2/code/{name of authorization client - in the sample: azure}`
This is also how the app registration should be configured.

### Authorities and Roles

When no roles assigned to a user logging in, the default list of Authorities just includes `ROLE_USER`.
When a role is assigned to the user logging in (for example a role with name `staff`), that will **NOT** show up as Authority however.  Rather one can fetch the role from the claims on the Principal, as follows:

~~~java
logger.info("roles found: {}", authentication.getPrincipal().getAttribute("roles").toString());
~~~

### Authorities and Groups

The groups who are allowed to log in can be defined in the application.yml as follows:

~~~yml
azure:
  activedirectory:
    tenant-id: xxx
    client-id: xxx
    client-secret: xxx
    post-logout-redirect-uri: xxx
    user-group:
      allowed-groups: 7d9e422c-b567-41a8-8ea2-52d86344d794, writers
~~~

It does not look like, with regular login, those groups are being picked up nor validated though.

## AAD Authorization (fetching OAuth2 Access Tokens)

## Troubleshooting

**Make sure the correct enviroment is used:**

To run the app with the environment called `local`:
`./mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.springframework=DEBUG --spring.profiles.active=local"`

**Tracing:**

Run the application with tracing enabled: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE`

**Using a proxy - THE BELOW DOES NOT WORK THOUGH IT SEEMS:**
Get the docker ip for localhost to connect from the dev container into Fidler locally:
`nslookup host.docker.internal`

Before being able to run through a proxy, the proxy's root cert needs to be trusted.  
To get this certificate (through a proxy): `openssl s_client -connect www.microsoft.com:443 -servername www.microsoft.com -showcerts -proxy 192.168.65.2:8888`.  Then copy the cert details and put them in a .crt file.

Then .crt certificate can be imported like this:
`$JAVA_HOME/bin/keytool -import -v -trustcacerts -alias fiddler -file /FiddlerRoot.cer -keystore cacerts.jks -keypass changeit -storepass changeit`

Run the application with proxy settings: `./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local" -Dhttp.proxyHost=192.168.65.2 -Dhttp.proxyPort=8888 -Dhttps.proxyHost=192.168.65.2 -Dhttps.proxyPort=8888`

## Resources

- [Implementation of Spring SDK in GH](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp)
- [Announcement on AAD integration v3.0.0](https://spring.io/blog/2021/01/13/the-latest-on-azure-active-directory-integration)
