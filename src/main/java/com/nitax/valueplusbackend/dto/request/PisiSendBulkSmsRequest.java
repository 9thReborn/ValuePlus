package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
@Data
public class PisiSendBulkSmsRequest {
    @NotEmpty(message = "Message cannot be empty")
    private String message;
    @NotEmpty(message = "Recipient cannot be empty")
    private String recipients;
    @NotEmpty(message = "Sender id cannot be empty")
    private String senderId;
}


//{"Success":true,"StatusCode":"1000","Message":"Successful","Provider":"NITAX TECHNOLOGY SMS","Pisi-authorization-token":"74aa5eaf-8854-4540-bb48-488ba7138583","Pisisid":27,"Expiration":"638804229012486685"}