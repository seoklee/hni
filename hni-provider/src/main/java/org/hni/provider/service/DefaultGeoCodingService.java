package org.hni.provider.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.http.client.fluent.Request;
import org.hni.user.om.Address;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultGeoCodingService implements GeoCodingService {

    private static final String GOOGLE_MAP_API_KEY = "AIzaSyBCmt3RMn46CIpxUx20hmlpPbx6ws-lbkI";

    private static final Configuration CONF = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonProvider())
            .build();

    @Override
    public Optional<Address> resolveAddress(String address) {

        Address addrPoint = null;

        String targetURI = null;
        try {
            targetURI = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    URLEncoder.encode(address, "UTF-8") + "&key=" + GOOGLE_MAP_API_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        try {
            String json = Request.Get(targetURI)
                    .execute()
                    .returnContent()
                    .asString();
            if (json != null) {
                DocumentContext ctx = JsonPath.using(CONF).parse(json);

                if ("OK".equals(ctx.read("status"))) {
                    Double latitude = ctx.read("$.results[0].geometry.location.lat");
                    Double longitude = ctx.read("$.results[0].geometry.location.lng");
                    TypeRef<List<AddressComponent>> type = new TypeRef<List<AddressComponent>>() {
                    };
                    List<AddressComponent> addressComponents = ctx.read("$.results[0].address_components", type);
                    addrPoint = new Address();
                    String streetNumber = getValue(addressComponents, "street_number");
                    addrPoint.setAddress1((streetNumber == null ? "" : (streetNumber + " ")) + getValue(addressComponents, "route"));
                    addrPoint.setCity(getValue(addressComponents, "locality"));
                    addrPoint.setState(getValue(addressComponents, "administrative_area_level_1"));
                    addrPoint.setZip(getValue(addressComponents, "postal_code"));
                    addrPoint.setLatitude(latitude);
                    addrPoint.setLongitude(longitude);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.ofNullable(addrPoint);

    }

    private String getValue(List<AddressComponent> addressComponents, String typeFilter) {
        Optional<AddressComponent> addr = addressComponents.stream().filter(ac -> ac.getTypes().contains(typeFilter)).findFirst();
        return addr.orElse(new AddressComponent()).getShortName();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AddressComponent {
        // a shortcut naming
        @JsonProperty("short_name")
        String shortName;
        List<String> types;

        public String getShortName() {
            return shortName;
        }

        public List<String> getTypes() {
            return types;
        }
    }
}
