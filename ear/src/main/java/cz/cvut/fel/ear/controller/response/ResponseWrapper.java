package cz.cvut.fel.ear.controller.response;

import cz.cvut.fel.ear.dto.BasicDTO;

import java.util.*;

public class ResponseWrapper {
    public enum ErrorMessageCode {
        INVALID_FIELD_TYPE ("invalid type in field %s"),
        INVALID_FIELD_VALUE ("invalid value in field %s"),
        MISSING_FIELD ("field %s is required"),
        NUMBER_OUT_OF_RANGE ("field %s is out of set range"),

        INVALID_DATE_FORMAT ("field %s has invalid date format"),
        INVALID_BODY_FORMAT ("the request body you sent has syntax error"),
        MISSING_BODY ("in your request there has been no body set"),

        INVALID_LOAN_NOT_APPROVED ("Loan cannot be returned because it isn't approved")
        ;

        private final String template;
        ErrorMessageCode(String template) { this.template = template; }
        public String format(String field) { return String.format(template, field); }
    }

    public enum ResponseInfoCode {
        SUCCESS_FOUND ("%s was found"),
        SUCCESS_CREATED ("%s was created"),
        SUCCESS_DELETED ("%s was successfully deleted"),
        SUCCESS_MODIFIED ("%s was successfully modified"),
        SUCCESS_ITEM_ADDED_TO_SOURCE ("%s was successfully added to %s"),
        SUCCESS_ITEM_REMOVED_FROM_SOURCE ("%s was successfully removed from %s"),

        ERROR_VALIDATION ("Validation error occurred"),

        ERROR_ALREADY_EXISTS ("%s already exists"),
        ERROR_ITEM_ALREADY_EXISTS_IN_RESOURCE ("%s already exists in %s"),
        ERROR_ENDPOINT_NOT_FOUND ("Endpoint not found"),
        ERROR_RESOURCE_IN_USE ("Resource cannot be deleted because it is being used"),

        ERROR_ITEM_NOT_FOUND ("%s not found"),
        ERROR_ITEM_NOT_IN_RESOURCE ("%s does not exist in %s"),
        ERROR_INVALID_REQUEST_FORMAT("Invalid request format"),
        ERROR_RESOURCE_NOT_FOUND("Requested resource not found"),
        ERROR_INTERNAL_SERVER_ERROR("Internal server error occurred"),

        DENIED_AUTHORIZATION ("Access denied"),

        INVALID_BODY_FORMAT ("the request body you sent has syntax error"),
        ERROR_METHOD_NOT_SUPPORTED("%s not supported is not supported at this endpoint"),

        INVALID_AUTHORIZATION ("username or password is incorrect")
        ;

        private final String template;
        ResponseInfoCode(String template) { this.template = template; }
        // we are using Object... to allow both String and other types to be passed
        public String format(Object... args) { return String.format(template, args); }
    }



    private Map<String, Object> responseData = new LinkedHashMap<>();
    private String responseInfoMessage;
    private List<Object> responseInfoErrors = new ArrayList<>();

    /**
     * Returns the full response build in building methods
     * @return full response
     */
    public Map<String, Object> getResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("responseData", this.responseData);

        Map<String, Object> responseInfo = new LinkedHashMap<>();
        responseInfo.put("message", responseInfoMessage);
        responseInfo.put("error", responseInfoErrors);

        response.put("responseInfo",responseInfo);

        return response;
    }

    /**
     * Used to add single item into response data
     * @param key Key under which it will be displayed in responseData
     * @param value
     */
    public void addResponseData(String key, Object value) {
        this.responseData.put(key, value);
    }

    /**
     * Used to add single DTO into the response
     * @param key Key under which it will be displayed in responseData
     * @param dto
     */
    public void addResponseData(String key, BasicDTO dto) {
        this.responseData.put(key, dto);
    }

    /**
     * Used to add multiple DTOs at once into the response
     * @param key Key under which all DTOs will be displayed in responseData
     * @param dtos
     */
    public void addResponseData(String key, List<? extends BasicDTO> dtos) {
        this.responseData.put(key, dtos);
    }

    /**
     * Sets ResponseInfoMessage - must pass correct amount of arguments for message to format
     * @param responseCode
     * @param args params to set to the message
     */
    public void setResponseInfoMessage(ResponseInfoCode responseCode, String... args) {
        this.responseInfoMessage = responseCode.format((Object[]) args);
    }

    /**
     * Adds new error to ResponseInfoError
     * @param errorCode
     * @param fieldName - field name where error occured
     */
    public void addResponseInfoError(ErrorMessageCode errorCode, String fieldName) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("message", errorCode.format(fieldName));
        error.put("fieldName", fieldName);

        this.responseInfoErrors.add(error);
    }
}
