/*
 * Copyright (C) 2014 Xform-Live contributors
 *
 * This file is part of Xform-Live.
 *
 * Xform-Live is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Xform-Live is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Xform-Live.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.kahowell.xformsvc;

import io.github.kahowell.xformsvc.model.Config;

import java.io.File;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

@Path("/config")
public class ConfigService {
    private static Logger log = Logger.getLogger(ConfigService.class.getCanonicalName());

    private static Config[] builtin = new Config[]{};
    static {
        try {
            String dataPath = System.getenv("OPENSHIFT_DATA_DIR"); // TODO don't couple here; allow deployment on other platforms
            if (dataPath == null) {
                dataPath = System.getenv("HOME"); // FIXME - fix for non-unix
            }
            File configFile = new File(dataPath + "/.xformliveconfig.json");
            log.info("Reading config from " + configFile.getAbsolutePath());

            ObjectMapper mapper = new ObjectMapper();
            builtin = mapper.readValue(configFile, Config[].class);

            for (Config config : builtin) {
                Config.replaceConfig(config);
            }
        }
        catch (Exception e) {
            log.warning(e.getMessage());
            log.info("Error reading config.json; app will operate in configurable mode");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{uuid}/download")
    public Response download(@PathParam("uuid") String uuid) {
        return Response.ok().header("Content-disposition", String.format("attachment; filename=%s.json", uuid)).entity(Config.getInstance(uuid)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Config newConfig() {
        return Config.newInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{uuid}")
    public Config getConfig(@PathParam("uuid") String uuid) {
        return Config.getInstance(uuid);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{uuid}")
    public void setConfig(@PathParam("uuid") String uuid, Config config) {
        Config.replaceConfig(config);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("builtin")
    public Config[] getBuiltin() {
        return builtin;
    }
}
