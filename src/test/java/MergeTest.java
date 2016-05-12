import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class MergeTest {
    TestSubscriber<Integer> ts = new TestSubscriber();
    TestScheduler scheduler = new TestScheduler();

    @Test
    public void testMerge() throws Exception {
        Observable<Integer> seq1 = Observable.just(1, 2, 3);
        Observable<Integer> seq2 = Observable.just(4, 5, 6);

        Observable.merge(seq1, seq2).subscribe(ts);

        assertThat(ts.getOnNextEvents(), contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    public void testMergeIntervals() throws Exception {
        TestSubscriber<Long> sub = new TestSubscriber<>();
        Observable<Long> int1 = Observable.interval(10, TimeUnit.MILLISECONDS, scheduler);
        Observable<Long> int2 = Observable.interval(10, TimeUnit.MILLISECONDS, scheduler);

        int1.mergeWith(int2).subscribe(sub);
        scheduler.advanceTimeBy(30, TimeUnit.MILLISECONDS);

        assertThat(sub.getOnNextEvents(), contains(0L, 0L, 1L, 1L, 2L, 2L));
    }

    // TODO: more merge tests
}
