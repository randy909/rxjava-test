import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SwitchTest {
    @Test
    public void testSwitch() throws Exception {
        TestSubscriber<String> ts = new TestSubscriber();
        Observable<Observable<String>> obsobs = Observable.just(
                Observable.just("one", "two"),
                Observable.just("three")
        );
        Observable.switchOnNext(obsobs).subscribe(ts);

        assertThat(ts.getOnNextEvents(), contains("one", "two", "three"));
    }

    @Test
    public void testSwitchKeepsGoingWhenFirstObservableCompletes() throws Exception {
        TestSubscriber<String> ts = new TestSubscriber();
        PublishSubject<Observable<String>> obsobs = PublishSubject.create();
        PublishSubject<String> first = PublishSubject.create();
        PublishSubject<String> second = PublishSubject.create();

        Observable.switchOnNext(obsobs).subscribe(ts);
        obsobs.onNext(first);
        first.onNext("one");
        first.onNext("two");
        first.onCompleted();
        obsobs.onNext(second);
        second.onNext("three");

        ts.assertNoErrors();
        assertThat(ts.getOnNextEvents(), contains("one", "two", "three"));
    }

    @Test
    public void testSwitchStopsWhenObservableFails() throws Exception {
        TestSubscriber<String> ts = new TestSubscriber();
        PublishSubject<Observable<String>> obsobs = PublishSubject.create();
        PublishSubject<String> first = PublishSubject.create();

        Observable.switchOnNext(obsobs).subscribe(ts);
        obsobs.onNext(first);
        first.onNext("one");
        Exception exception = new Exception("hi");
        first.onError(exception);

        assertThat(ts.getOnErrorEvents(), contains(exception));
    }

    @Test
    public void testSwitchDoesNotEmitAfterSwitch() throws Exception {
        TestSubscriber<String> ts = new TestSubscriber();
        PublishSubject<Observable<String>> obsobs = PublishSubject.create();
        PublishSubject<String> first = PublishSubject.create();
        PublishSubject<String> second = PublishSubject.create();

        Observable.switchOnNext(obsobs).subscribe(ts);
        obsobs.onNext(first);
        first.onNext("one");
        obsobs.onNext(second);
        first.onNext("two"); // ignored
        second.onNext("three");

        ts.assertNoErrors();
        assertThat(ts.getOnNextEvents(), contains("one", "three"));
    }
}
