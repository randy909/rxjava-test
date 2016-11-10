import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

public class OperatorTest {

    private final TestSubscriber ts = new TestSubscriber();

    @Test
    public void testImplementMap() throws Exception {

        Observable.just("hi")
                .lift(new MyMapOperator<>(s -> s + " there"))
                .subscribe(ts);

        ts.assertValue("hi there");
    }

    @Test
    public void testDistinctUntilChanged() throws Exception {
        Observable.just("hi", "hi", "hi", null, null, "there")
                .distinctUntilChanged()
                .lift(new MyDucOperator<>())
                .subscribe(ts);

        ts.assertValues("hi", null, "there");
    }

}

class MyMapOperator<T, R> implements Observable.Operator<T, R> {

    private Func1<R, T> func;

    public MyMapOperator(Func1<R, T> func) {
        this.func = func;
    }

    @Override
    public Subscriber<? super R> call(Subscriber<? super T> subscriber) {
        return new Subscriber<R>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(R r) {
                subscriber.onNext(func.call(r));
            }
        };
    }
}

class MyDucOperator<T> implements Observable.Operator<T, T> {
    private T last;

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> subscriber) {
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(T t) {
                System.out.println("last: " + last + " next: " + t);
                if (last != null) {
                    if (!last.equals(t)) {
                        subscriber.onNext(t);
                    }
                } else if (t != null) {
                    subscriber.onNext(t);
                }
                last = t;
            }
        };
    }
}
