package uk.oczadly.karl.csgsi;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Karl Oczadly
 */
public class ListenerRegistryTest {
    
    @Test
    public void testRegister() {
        ListenerRegistry reg = new ListenerRegistry();
        MockListener obs1 = new MockListener(), obs2 = new MockListener();
        assertEquals(0, reg.size());
        reg.register(obs1);
        reg.register(obs2);
        assertEquals(2, reg.size());
        assertTrue(reg.listeners.contains(obs1));
        assertTrue(reg.listeners.contains(obs2));
    }
    
    @Test
    public void testRegisterMulti() {
        ListenerRegistry reg = new ListenerRegistry();
        MockListener obs1 = new MockListener(), obs2 = new MockListener();
        assertEquals(0, reg.size());
        reg.register(Set.of(obs1, obs2));
        assertEquals(2, reg.size());
        assertTrue(reg.listeners.contains(obs1));
        assertTrue(reg.listeners.contains(obs2));
    }
    
    @Test
    public void testRemove() {
        ListenerRegistry reg = new ListenerRegistry();
        MockListener obs1 = new MockListener(), obs2 = new MockListener();
        reg.register(Set.of(obs1, obs2));
        reg.remove(obs2);
        assertEquals(1, reg.size());
    }
    
    @Test
    public void testClear() {
        ListenerRegistry reg = new ListenerRegistry();
        MockListener obs1 = new MockListener(), obs2 = new MockListener();
        reg.register(Set.of(obs1, obs2));
        reg.clear();
        assertEquals(0, reg.size());
    }
    
    @Test //TODO: doesn't test context object is the same
    public void testNotify() throws InterruptedException {
        ListenerRegistry reg = new ListenerRegistry();
        CountDownLatch obsLatch = new CountDownLatch(2);
        MockListener obs1 = new MockListener(obsLatch), obs2 = new MockListener(obsLatch);
        reg.register(Set.of(obs1, obs2));
        GameState mockState = new GameState();
        reg.notify(mockState, null); // Notify
        assertTrue(obsLatch.await(1, TimeUnit.SECONDS)); // Wait for notification
        assertTrue(obs1.called && obs2.called);
        assertSame(mockState, obs1.state);
        assertSame(mockState, obs2.state);
    }
    
}