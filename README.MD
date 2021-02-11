# Authentication and Authorization with Spring and Azure Active Directory

## AAD Pure Authentication (logging in with OIDC) - sample: "AAD3Auth"

> **Remarks**
>
> - Method annotations don't seem to work (for example `@PreAuthorize("permitAll()")` on `AuthenticatedPage`)
> - In the `configure` method, one cannot seem to configure ".anyRequest()" as this is already done by `super.configure(http)`.  In that case need to override everything?
> - AAD roles do not automatically translate into an Authority (does not seem [to be implemented here](https://github.com/Azure/azure-sdk-for-java/blob/c92f8071a051b9b6212529ac86955c5e972b9f16/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADOAuth2UserService.java#L54))
> - Is calling of `super.configure(http)` required?
> - Are groups supposed to be picked up as authorities from the `id_token` from  pure OIDC login?  ([This line](https://github.com/Azure/azure-sdk-for-java/blob/c92f8071a051b9b6212529ac86955c5e972b9f16/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADOAuth2UserService.java#L67) seems to suggest the logic always still goes out to the Graph to fetch group membership info.)

Just authenticating with AAD does not require any authorization clients to be configured.  A configuration similar to the below is enough:

~~~yml
azure:
  activedirectory:
    tenant-id: 3fd11e85-d8ce-4c7f-b6a0-816346615777
    client-id: 35cea567-1674-40d4-95a4-e0bf5314f89f
    client-secret: xxx
~~~

### Flow

- when browser visits a protected page, a redirect happens to: `/oauth/authorization/azure`
- this causes Spring to reply with a redirect to AAD on `https://login.microsoftonline.com/xxx/oauth2/v2.0/authorize?response_type=code&client_id=xxx&scope=openid%20profile%20https://graph.microsoft.com/User.Read%20https://graph.microsoft.com/Directory.AccessAsUser.All&state=xxx&redirect_uri=http://localhost:8080/login/oauth2/code/azure&nonce=xxx`
  - this means: an authcode flow asking for graph scopes: User.Read and Directory.AccessAsUser.All
- After login authcode is passed back into localhost web app: `http://localhost:8080/login/oauth2/code/azure?code=XXX`.  Note that this is also how app registration should be configured.
- The SDK will exchange the code for both id_token and access_token, again asking for scopes `openid profile https://graph.microsoft.com/User.Read https://graph.microsoft.com/Directory.AccessAsUser.All`
  - in the access token (for the graph) there will not be any groups, as that app registration would not have been configured for this
  - in the id_token there will be group (and role) information for the user if configured as such - however that is ignored by the SDK
- the SDK will then take the access token and use it to access the graph with asking the group membership on `https://graph.microsoft.com/v1.0/me/memberOf`
- That page will redirect to the protected page one wanted to visit.


### Authorities and Roles

When no roles assigned to a user logging in, the default list of Authorities just includes `ROLE_USER`.
When a role is assigned to the user logging in (for example a role with name `staff`), that will **NOT** show up as Authority however.  Rather one can fetch the role from the claims on the Principal, as follows:

~~~java
logger.info("roles found: {}", authentication.getPrincipal().getAttribute("roles").toString());
~~~

### Authorities and Groups

The groups for whom to create an authority can be defined in the application.yml as follows:

~~~yml
azure:
  activedirectory:
    tenant-id: xxx
    client-id: xxx
    client-secret: xxx
    post-logout-redirect-uri: xxx
    user-group:
      allowed-groups: Readers, writers # Note: this is case sensitive - compare this to the value coming back from graph call
~~~

Defining any group in here will not prevent the user from logging in, rather it will cause an authority to be created for any group the user is part of and is listed in the allowd-groups list.  Such authority will be named: `ROLE_Readers` for a group with name "Readers" for example.

## AAD Authorization (fetching OAuth2 Access Tokens)

## Troubleshooting

**Make sure the correct enviroment is used:**

To run the app with the environment called `local`:
`./mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.springframework=DEBUG --spring.profiles.active=local"`

**Tracing:**

Run the application with tracing enabled: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE`

**Using a proxy:**
Get the docker ip for localhost to connect from the dev container into Fidler locally:
`nslookup host.docker.internal`

Before being able to run through a proxy, the proxy's root cert needs to be trusted.  
To get this certificate, export it from within Fiddler.

Then .cer certificate can be imported like this:
`$JAVA_HOME/bin/keytool.exe -import -file C:\Users\<Username>\Desktop\FiddlerRoot.cer -keystore FiddlerKeystore -alias Fiddler`

Run the application with proxy settings: `./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local" -Dcom.sun.net.ssl.checkRevocation=false -Dtrust_all_cert=true -Dhttp.proxyHost=192.168.65.2 -Dhttp.proxyPort=8888 -Dhttps.proxyHost=192.168.65.2 -Dhttps.proxyPort=8888 -Djavax.net.ssl.trustStore=/workspaces/xxxx/FiddlerKeystore -Djavax.net.ssl.trustStorePassword=changeit`

For more info see also: https://stackoverflow.com/questions/8549749/how-to-capture-https-with-fiddler-in-java

## Resources

- [Implementation of Spring SDK in GH](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp)
- [Announcement on AAD integration v3.0.0](https://spring.io/blog/2021/01/13/the-latest-on-azure-active-directory-integration)

# Feedback Summary
- Login is not possible without access token it seems
- Admin consent is still required; this requirement should be lifted.  We see that the redirect to AAD looks like `https://login.microsoftonline.com/3fd11e85-d8ce-4c7f-b6a0-816346615777/oauth2/v2.0/authorize?response_type=code&client_id=35cea567-1674-40d4-95a4-e0bf5314f89f&`**scope=**`openid%20profile%20https://graph.microsoft.com/User.Read%20`**https://graph.microsoft.com/Directory.AccessAsUser.All**`&state=4q8qJIOav-gZFikcYKff1tBoel3RRUurqW5GHk74y-k%3D&redirect_uri=http://localhost:8080/login/oauth2/code/azure&nonce=I4KxJdTh7c6_fYXv2f4W20IZnSYla4g_jW2ph9C1xiQ`
  - The `AccessAsUser` scope is very broad, requires admin consent and should not be required for large majority of scenarios
- Groups are not picked up or checked from id_token but still come from a call to the graph.
  - Group membership can be read from the id_token without need for access token or more privileged access and/or admin consent
  - Groups are just translated into Authorities; we don't stop users based on group membership which is assumed is by design?
- Roles are not picked up or checked from id_token