#  Publication and Information Data Management APIM API

This is the configuration for setting up the Publication and Information Data Management API Management API, Operations and Policies


## Swagger

The API and the Operations are deployed using Swagger/Open API importing.

Any new Operation for the API should be added to the Swagger definition `infrastructure\resources\swagger\api-swagger.json`

## API Policies

The policies for the APIs are located `infrastructure\resources\api-policy`
These policies will affect all of the actions for the Data Management API.

## Operation Policies

The policies for the Operations are located `infrastructure\resources\operation-policies`
These policies will affect all of the actions for the Data Management API Operations.

### Add new Policy
To add a new policy, create the new XML file within the source location with the name of the Operation ID.
This should be the Operation ID set in the Swagger Definition and the new file name.

The content should start by looking like this:
```XML
<policies>
    <inbound>
        <base />
    </inbound>
    <backend>
        <base />
    </backend>
    <outbound>
        <base />
    </outbound>
    <on-error>
        <base />
    </on-error>
</policies>
```

## Testing

Testing is done via the Java Gradle, which is located in `./src` and then the respective testing folder.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
