import org.junit.Test;

import rx.Observable;
import rx.observables.GroupedObservable;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class GroupByTest {
    @Test
    public void testGroupBy() throws Exception {
        TestSubscriber<Integer> ts1 = new TestSubscriber<>();
        TestSubscriber<Integer> ts2 = new TestSubscriber<>();
        Observable<GroupedObservable<Integer, Integer>> groupedObservableObservable =
            Observable.just(1, 2, 1, 2)
                .groupBy(item -> item);

        groupedObservableObservable
                .subscribe(observable -> {
                    if (observable.getKey() == 1) {
                        observable.subscribe(ts1);
                    } else {
                        observable.subscribe(ts2);
                    }
                });

        assertThat(ts1.getOnNextEvents(), contains(1, 1));
        assertThat(ts2.getOnNextEvents(), contains(2, 2));
    }

    @Test
    public void testGroupByWithTransformer() throws Exception {
        TestSubscriber<Integer> ts = new TestSubscriber<>();

        Observable.just(1, 2, 1, 2)
                .groupBy(item -> item, item -> item + 10)
                .subscribe(observable -> {
                    if (observable.getKey() == 1) {
                        observable.subscribe(ts);
                    }
                });

        assertThat(ts.getOnNextEvents(), contains(11, 11));
    }
}
