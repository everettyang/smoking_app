package edu.tamu.geoinnovation.fpx.Modules;

/**
 * Created by atharmon on 10/9/2015.
 */

public class BasicServerResponse {
    public String Result;
    public String ResultMessage;
    public String ResultCode;
    public String Status;
    public String userGuid;

    public String toString() {
        return Status + " " + ResultMessage;
    }

}
