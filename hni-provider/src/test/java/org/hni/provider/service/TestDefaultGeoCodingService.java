package org.hni.provider.service;

import org.hni.user.om.Address;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Created by walmart on 11/14/16.
 */
public class TestDefaultGeoCodingService {

    DefaultGeoCodingService geoCodingService = new DefaultGeoCodingService();

    @Test
    public void simpleAddress() {
        Optional<Address> address = geoCodingService.resolveAddress("10790+Parkridge+Boulevard%2C+Reston%2C+VA+20191");
        Assert.assertTrue(address.isPresent());
    }

    @Test
    public void invalidAddress() {
        Optional<Address> address = geoCodingService.resolveAddress("JJJeHHH");
        Assert.assertFalse(address.isPresent());
    }

    @Test
    public void invalidAddress2() {
        Optional<Address> address = geoCodingService.resolveAddress("7273477383883884");
        Assert.assertFalse(address.isPresent());
    }
}
