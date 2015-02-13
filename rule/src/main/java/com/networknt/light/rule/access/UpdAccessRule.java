package com.networknt.light.rule.access;

import com.networknt.light.rule.Rule;
import com.networknt.light.rule.RuleEngine;
import com.networknt.light.rule.rule.AbstractRuleRule;
import com.networknt.light.server.DbService;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Created by steve on 30/01/15.
 */
public class UpdAccessRule extends AbstractAccessRule implements Rule {
    public boolean execute (Object ...objects) throws Exception {
        Map<String, Object> inputMap = (Map<String, Object>)objects[0];
        Map<String, Object> data = (Map<String, Object>)inputMap.get("data");
        Map<String, Object> payload = (Map<String, Object>) inputMap.get("payload");
        int inputVersion = (int)data.get("@version");
        String rid = (String)data.get("@rid");
        String ruleClass = (String)data.get("ruleClass");
        String error = null;
        if(payload == null) {
            error = "Login is required";
            inputMap.put("responseCode", 401);
        } else {
            Map<String, Object> user = (Map<String, Object>)payload.get("user");
            List roles = (List)user.get("roles");
            if(!roles.contains("owner") && !roles.contains("admin") && !roles.contains("ruleAdmin")) {
                error = "Role owner or admin or ruleAdmin is required to update access control";
                inputMap.put("responseCode", 403);
            } else {
                String host = (String)user.get("host");
                if(host != null) {
                    if(!host.equals(data.get("host"))) {
                        error = "User can only update access control for host: " + host;
                        inputMap.put("responseCode", 403);
                    } else {
                        // make sure the ruleClass contains the host.
                        if(host!=null && !ruleClass.contains(host)) {
                            // you are not allowed to update access control to the rule as it is not owned by the host.
                            error = "ruleClass is not owned by the host: " + host;
                            inputMap.put("responseCode", 403);
                        } else {
                            ODocument access = DbService.getODocumentByRid(rid);
                            if(access == null) {
                                error = "Access control with @rid " + rid + " cannot be found";
                                inputMap.put("responseCode", 404);
                            } else {
                                int storedVersion = access.field("@version");
                                if(inputVersion != storedVersion) {
                                    error = "Updating version " + inputVersion + " doesn't match stored version " + storedVersion;
                                    inputMap.put("responseCode", 400);
                                } else {
                                    // make sure the data passed in make sense based on accessLevel.
                                    Map eventMap = getEventMap(inputMap);
                                    Map<String, Object> eventData = (Map<String, Object>)eventMap.get("data");
                                    inputMap.put("eventMap", eventMap);
                                    eventData.put("ruleClass", ruleClass);
                                    String accessLevel = (String)data.get("accessLevel");
                                    eventData.put("accessLevel", accessLevel);
                                    List clients = (List)data.get("clients");
                                    roles = (List)data.get("roles");
                                    List users = (List)data.get("users");
                                    switch (accessLevel) {
                                        case "A":
                                            // Access by anyone, ignore clients, roles and users
                                            break;
                                        case "N":
                                            // Not accessible, ignore clients, roles and users.
                                            break;
                                        case "C":
                                            // client id is in the jwt token like userId and roles.
                                            if(clients == null || clients.size() == 0 ) {
                                                error = "Clients are empty for client based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("clients", clients);
                                            }
                                            break;
                                        case "R":
                                            // role only
                                            if(roles == null || roles.size() == 0 ) {
                                                error = "Roles are empty for role based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("roles", roles);
                                            }
                                            break;
                                        case "U":
                                            //user only
                                            if(users == null || users.size() == 0 ) {
                                                error = "Users are empty for user based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("users", users);
                                            }
                                            break;
                                        case "CR":
                                            // client and role
                                            if(clients == null || clients.size() == 0 || roles == null || roles.size() == 0) {
                                                error = "Clients or roles are empty for client and role based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("clients", clients);
                                                eventData.put("roles", roles);
                                            }
                                            break;
                                        case "CU":
                                            // client and user
                                            if(clients == null || clients.size() == 0 || users == null || users.size() == 0) {
                                                error = "Clients or users are empty for client and user based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("clients", clients);
                                                eventData.put("users", users);
                                            }
                                            break;
                                        case "RU":
                                            // role and user
                                            if(roles == null || roles.size() == 0 || users == null || users.size() == 0) {
                                                error = "Roles or users are empty for role and user based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("roles", roles);
                                                eventData.put("users", users);
                                            }
                                            break;
                                        case "CRU":
                                            // client, role and user
                                            if(clients == null || clients.size() == 0 || roles == null || roles.size() == 0 || users == null || users.size() == 0) {
                                                error = "Clients, roles or users are empty for client, role and user based access control";
                                                inputMap.put("responseCode", 400);
                                            } else {
                                                eventData.put("clients", clients);
                                                eventData.put("roles", roles);
                                                eventData.put("users", users);
                                            }
                                            break;
                                        default:
                                            logger.error("Invalid Access Level: " + accessLevel);
                                    }
                                    eventData.put("updateDate", new java.util.Date());
                                    eventData.put("updateUserId", user.get("userId"));
                                }
                            }
                        }
                    }
                } else {
                    // this is the owner.
                    ODocument access = DbService.getODocumentByRid(rid);
                    if(access == null) {
                        error = "Access control with @rid " + rid + " cannot be found";
                        inputMap.put("responseCode", 404);
                    } else {
                        int storedVersion = access.field("@version");
                        if(inputVersion != storedVersion) {
                            error = "Updating version " + inputVersion + " doesn't match stored version " + storedVersion;
                            inputMap.put("responseCode", 400);
                        } else {
                            Map eventMap = getEventMap(inputMap);
                            Map<String, Object> eventData = (Map<String, Object>)eventMap.get("data");
                            inputMap.put("eventMap", eventMap);
                            eventData.put("ruleClass", ruleClass);
                            String accessLevel = (String)data.get("accessLevel");
                            eventData.put("accessLevel", accessLevel);
                            List clients = (List)data.get("clients");
                            roles = (List)data.get("roles");
                            List users = (List)data.get("users");
                            switch (accessLevel) {
                                case "A":
                                    // Access by anyone, ignore clients, roles and users
                                    break;
                                case "N":
                                    // Not accessible, ignore clients, roles and users.
                                    break;
                                case "C":
                                    // client id is in the jwt token like userId and roles.
                                    if(clients == null || clients.size() == 0 ) {
                                        error = "Clients are empty for client based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("clients", clients);
                                    }
                                    break;
                                case "R":
                                    // role only
                                    if(roles == null || roles.size() == 0 ) {
                                        error = "Roles are empty for role based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("roles", roles);
                                    }
                                    break;
                                case "U":
                                    //user only
                                    if(users == null || users.size() == 0 ) {
                                        error = "Users are empty for user based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("users", users);
                                    }
                                    break;
                                case "CR":
                                    // client and role
                                    if(clients == null || clients.size() == 0 || roles == null || roles.size() == 0) {
                                        error = "Clients or roles are empty for client and role based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("clients", clients);
                                        eventData.put("roles", roles);
                                    }
                                    break;
                                case "CU":
                                    // client and user
                                    if(clients == null || clients.size() == 0 || users == null || users.size() == 0) {
                                        error = "Clients or users are empty for client and user based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("clients", clients);
                                        eventData.put("users", users);
                                    }
                                    break;
                                case "RU":
                                    // role and user
                                    if(roles == null || roles.size() == 0 || users == null || users.size() == 0) {
                                        error = "Roles or users are empty for role and user based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("roles", roles);
                                        eventData.put("users", users);
                                    }
                                    break;
                                case "CRU":
                                    // client, role and user
                                    if(clients == null || clients.size() == 0 || roles == null || roles.size() == 0 || users == null || users.size() == 0) {
                                        error = "Clients, roles or users are empty for client, role and user based access control";
                                        inputMap.put("responseCode", 400);
                                    } else {
                                        eventData.put("clients", clients);
                                        eventData.put("roles", roles);
                                        eventData.put("users", users);
                                    }
                                    break;
                                default:
                                    logger.error("Invalid Access Level: " + accessLevel);
                            }
                            eventData.put("updateDate", new java.util.Date());
                            eventData.put("updateUserId", user.get("userId"));
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