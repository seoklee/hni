package org.hni.order.dao;

import org.junit.Assert;
import org.hni.order.om.PartialOrder;
import org.hni.order.om.TransactionPhase;
import org.hni.provider.om.MenuItem;
import org.hni.provider.om.ProviderLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Verifies that the PartialOrder DAO is working
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml", "classpath:redis.ctx.xml"} )
@Transactional
public class TestDefaultPartialOrderDAO {

    @Inject
    private DefaultPartialOrderDAO partialOrderDAO;

    @Test
    public void testPartialOrderDao_verify() {
        PartialOrder partialOrder = new PartialOrder();

        partialOrder.setChosenProvider(new ProviderLocation(1L));
        partialOrder.setMenuItemsForSelection(Arrays.asList(new MenuItem(1L)));
        Set set = new HashSet<>();
        set.add(new MenuItem(1L));
        partialOrder.setMenuItemSelected(set);
        partialOrder.setProviderLocationsForSelection(Arrays.asList(new ProviderLocation(2L)));
        partialOrder.setTransactionPhase(TransactionPhase.CONFIRM_OR_REDO);

        partialOrderDAO.save(partialOrder);
        PartialOrder orderReceived = (PartialOrder) partialOrderDAO.get(1L);

        Assert.assertEquals(orderReceived, partialOrder);
        Assert.assertEquals(orderReceived.getChosenProvider(), partialOrder.getChosenProvider());
        Assert.assertEquals(orderReceived.getMenuItemsForSelection(), partialOrder.getMenuItemsForSelection());
        Assert.assertEquals(orderReceived.getMenuItemsSelected(), partialOrder.getMenuItemsSelected());
        Assert.assertEquals(orderReceived.getProviderLocationsForSelection(), partialOrder.getProviderLocationsForSelection());
        Assert.assertEquals(orderReceived.getTransactionPhase(), partialOrder.getTransactionPhase());
    }
}
