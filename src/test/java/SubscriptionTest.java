import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SubscriptionTest {
    TestSubscriber ts = new TestSubscriber();

    @Test
    public void testSubscribingToChainedObservableSubscribesToParent() throws Exception {
        PublishSubject subject = PublishSubject.create();
        Observable strObservable = subject.map(Object::toString);

        strObservable.subscribe(ts);
        assertThat(subject.hasObservers(), is(true));
    }

}
