package com.networknt.light.rule.user;

import com.networknt.light.rule.Rule;
import com.networknt.light.server.DbService;
import com.networknt.light.util.ServiceLocator;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by husteve on 8/29/2014.
 */
public class DelUserRule extends AbstractUserRule implements Rule {
    public boolean execute (Object ...objects) throws Exception {
        Map<String, Object> inputMap = (Map<String, Object>) objects[0];
        Map<String, Object> data = (Map<String, Object>) inputMap.get("data");
        String rid = (String) data.get("@rid");
        String userId = (String)data.get("userId");
        String error = null;
        Map<String, Object> payload = (Map<String, Object>) inputMap.get("payload");
        if(payload == null) {
            error = "Login is required";
            inputMap.put("responseCode", 401);
        } else {
            Map<String, Object> user = (Map<String, Object>)payload.get("user");
            List roles = (List)user.get("roles");
            if(!roles.contains("owner") && !roles.contains("admin") && !roles.contains("userAdmin")) {
                error = "Role owner or admin or userAdmin is required to delete user";
                inputMap.put("responseCode", 401);
            } else {
                String host = (String)user.get("host");
                if(host != null && !host.equals(data.get("host"))) {
                    error = "User can only delete user from host: " + host;
                    inputMap.put("responseCode", 401);
                } else {
                    ODocument deleteUser = null;
                    if(rid == null && userId == null) {
                        inputMap.put("error", "@rid or userId is required");
                        inputMap.put("responseCode", 400);
                    } else {
                        if(rid != null) {
                            deleteUser = DbService.getODocumentByRid(rid);
                            if(deleteUser == null) {
                                error = "User with rid " + rid + " cannot be found.";
                                inputMap.put("responseCode", 404);
                            }
                        } else {
                            deleteUser = getUserByUserId(userId);
                            if(deleteUser == null) {
                                error = "User with userId " + userId + " cannot be found.";
                                inputMap.put("responseCode", 404);
                            }
                        }
                        if(deleteUser != null) {
                            Map eventMap = getEventMap(inputMap);
                            Map<String, Object> eventData = (Map<String, Object>)eventMap.get("data");
                            inputMap.put("eventMap", eventMap);
                            eventData.put("userId", deleteUser.field("userId").toString());
                        }
                    }
                }
            }
        }
        if(error != null) {
            inputMap.put("error", error);
            return false;
        } else {
            return true;
        }
    }
}
