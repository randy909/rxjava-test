import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class ErrorTest {

    private RuntimeException exception = new RuntimeException("bad");
    private PublishSubject<String> subject = PublishSubject.create();
    private TestSubscriber<String> ts = new TestSubscriber<>();
    private TestSubscriber<String> explodingSub = new TestSubscriber<String>() {
        @Override
        public void onNext(String s) {
            super.onNext(s);
            throw exception;
        }
    };

    @Before
    public void setUp() throws Exception {
        subject.subscribe(explodingSub);
    }

    @Test
    public void testSubscriberDoestGetNotificationsAfterError() throws Exception {
        subject.onNext("explode");
        subject.onNext("ignored after error");

        explodingSub.assertValue("explode");
        explodingSub.assertError(exception);
    }

    @Test
    public void testOnCompletedIsNotCalledWhenSubscriberThrows() throws Exception {
        subject.onNext("explode");

        explodingSub.assertNotCompleted();
    }

    @Test
    public void testSubjectIsUnsubscribedFromWhenSubscriberThrows() throws Exception {
        assertThat(subject.hasObservers(), is(true));

        subject.onNext("explode");

        assertThat(subject.hasObservers(), is(false));
    }

    @Test
    public void testNewSubscriberIsNotAffectedByPreviousError() throws Exception {
        subject.onNext("explode");

        subject.subscribe(ts);
        subject.onNext("hi");

        assertThat(ts.getOnNextEvents(), contains("hi"));
    }

    @Test
    public void testSimultaneousSubscriberIsNotAffectedByError() throws Exception {
        subject.subscribe(ts);

        subject.onNext("one"); // explodingSub explodes
        subject.onNext("two");

        ts.assertNoTerminalEvent();
        assertThat(ts.getOnNextEvents(), contains("one", "two"));
        assertThat(subject.hasObservers(), is(true));
    }

    @Test
    public void testOnErrorUnsubscribesAllSubscribers() throws Exception {
        subject.subscribe(ts);

        subject.onError(exception);

        assertThat(subject.hasObservers(), is(false));
        explodingSub.assertError(exception);
        ts.assertError(exception);
    }

    @Test
    public void testAllSubsequentSubscribersGetErrorAfterOnError() throws Exception {
        subject.onError(exception);

        subject.subscribe(ts);

        ts.assertError(exception);
    }

    @Test
    public void testDoOnErrorDoesNotGetCalledWhenSubscriberThrows() throws Exception {
        PublishSubject<String> sub = PublishSubject.<String>create();
        AtomicBoolean called = new AtomicBoolean(false);
        sub.doOnError(e -> called.set(true))
           .subscribe(explodingSub);

        sub.onNext("hi");

        assertThat(called.get(), is(false));
    }

    @Test
    public void testDoOnErrorGetsCalledWhenOnErrorCalled() throws Exception {
        PublishSubject<String> sub = PublishSubject.<String>create();
        AtomicBoolean called = new AtomicBoolean(false);
        sub.doOnError(e -> called.set(true))
                .subscribe(explodingSub);

        sub.onError(exception);

        assertThat(called.get(), is(true));
    }

    // TODO: what happens upstream/downstream of an exploding map (doOnError before and after)

    // TODO test these subject methods, hasBlah()...
    // assertThat(subject.hasThrowable(), is(true));
}
