# Authentication and Authorization with Spring and Azure Active Directory

## AAD Pure Authentication (logging in with OIDC)

> **Remarks**
>
> - Method annotations don't seem to work
> - In the `configure` method, one cannot seem to configure ".anyRequest()" as this is already done by `super.configure(http)`
> - Is calling of `super.configure(http)` required?
>

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

## AAD Authorization (fetching OAuth2 Access Tokens)

## Troubleshooting

**Make sure the correct enviroment is used:**

To run the app with the environment called `local`:
`./mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.springframework=DEBUG --spring.profiles.active=local"`

**Tracing:**

Run the application with tracing enabled: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE`

## Resources

- [Implementation of Spring SDK in GH](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp)
- [Announcement on AAD integration v3.0.0](https://spring.io/blog/2021/01/13/the-latest-on-azure-active-directory-integration)
