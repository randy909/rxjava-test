import org.junit.Before;
import org.junit.Test;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class ErrorTest {

    private RuntimeException exception = new RuntimeException("bad");
    private PublishSubject<String> subject = PublishSubject.create();
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

        assertThat(explodingSub.getOnNextEvents(), contains("explode"));
        assertThat(explodingSub.getOnErrorEvents(), contains(exception));
    }

    @Test
    public void testOnCompletedIsNotCalledWhenSubscriberThrows() throws Exception {
        subject.onNext("explode");

        assertThat(explodingSub.getOnCompletedEvents().size(), is(0));
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

        TestSubscriber<String> ts = new TestSubscriber<>();
        subject.subscribe(ts);
        subject.onNext("hi");

        assertThat(ts.getOnNextEvents(), contains("hi"));
    }

    // TODO test these subject methods, hasBlah()...
    // assertThat(subject.hasThrowable(), is(true));
}
