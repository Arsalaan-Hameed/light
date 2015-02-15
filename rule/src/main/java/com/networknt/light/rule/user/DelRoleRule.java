/*
 * Copyright 2015 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.light.rule.user;

import com.networknt.light.rule.Rule;
import com.networknt.light.server.DbService;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.List;
import java.util.Map;

/**
 * Created by husteve on 10/17/2014.
 *
 * Delete a role from a user
 *
 * AccessLevel R [owner, admin, userAdmin]
 *
 */
public class DelRoleRule extends AbstractUserRule implements Rule {
    public boolean execute (Object ...objects) throws Exception {
        Map<String, Object> inputMap = (Map<String, Object>) objects[0];
        Map<String, Object> data = (Map<String, Object>) inputMap.get("data");
        Map<String, Object> payload = (Map<String, Object>) inputMap.get("payload");
        Map<String, Object> user = (Map<String, Object>)payload.get("user");
        String rid = (String) data.get("@rid");
        String role = (String) data.get("role");
        String error = null;
        String host = (String)user.get("host");
        if(host != null && !host.equals(data.get("host"))) {
            error = "User can only delete role from host: " + host;
            inputMap.put("responseCode", 401);
        } else {
            ODocument updateUser = DbService.getODocumentByRid(rid);
            if(updateUser != null) {
                List updateRoles = (List)updateUser.field("roles");
                if(!updateRoles.contains(role)) {
                    error = "User doesn't have role: " + role;
                    inputMap.put("responseCode", 400);
                } else {
                    Map eventMap = getEventMap(inputMap);
                    Map<String, Object> eventData = (Map<String, Object>)eventMap.get("data");
                    inputMap.put("eventMap", eventMap);
                    eventData.put("userId", updateUser.field("userId"));
                    eventData.put("role", role);
                    eventData.put("updateDate", new java.util.Date());
                    eventData.put("updateUserId", user.get("userId"));
                }
            } else {
                error = "User with @rid " + rid + " cannot be found.";
                inputMap.put("responseCode", 404);
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
