import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

public class LiftTest {
    TestSubscriber ts = new TestSubscriber();

    // TODO: figure this confusing crap out (internet is down! :()
    @Test
    public void testLift() throws Exception {
        Observable<Integer> nums = Observable.just(1, 2, 3);

        Observable<Object> lifted = nums.lift(new Observable.Operator<Object, Integer>() {
            @Override
            public Subscriber<? super Integer> call(Subscriber<? super Object> subscriber) {
                return null;
            }
        });
        lifted.subscribe(ts);

        System.out.println(ts.getOnNextEvents());
    }
}
