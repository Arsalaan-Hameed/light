package com.networknt.light.rule.rule;

import com.networknt.light.rule.Rule;
import com.networknt.light.rule.role.AbstractRoleRule;
import com.networknt.light.server.DbService;

import java.util.List;
import java.util.Map;

/**
 * Created by steve on 07/11/14.
 */
public class DelRuleRule extends AbstractRuleRule implements Rule {
    public boolean execute (Object ...objects) throws Exception {
        Map<String, Object> inputMap = (Map<String, Object>) objects[0];
        Map<String, Object> data = (Map<String, Object>) inputMap.get("data");
        Map<String, Object> payload = (Map<String, Object>) inputMap.get("payload");
        String rid = (String)data.get("@rid");
        String error = null;
        if(payload == null) {
            error = "Login is required";
            inputMap.put("responseCode", 401);
        } else {
            Map<String, Object> user = (Map<String, Object>)payload.get("user");
            List roles = (List)user.get("roles");
            if(!roles.contains("owner") && !roles.contains("admin") && !roles.contains("ruleAdmin")) {
                error = "Role owner or admin or ruleAdmin is required to delete rule";
                inputMap.put("responseCode", 401);
            } else {
                String host = (String)user.get("host");
                if(host != null && !host.equals(data.get("host"))) {
                    error = "User can only delete rule for host: " + host;
                    inputMap.put("responseCode", 401);
                } else {
                    String json = DbService.getJsonByRid(rid);
                    if(json == null) {
                        error = "Rule with @rid " + rid + " cannot be found";
                        inputMap.put("responseCode", 404);
                    } else {
                        // TODO check if rule is disabled and version is matched.

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
