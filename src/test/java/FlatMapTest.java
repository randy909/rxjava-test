import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class FlatMapTest {

    TestSubscriber ts = new TestSubscriber();

    @Test
    public void testNoReason() throws Exception {
        Observable.just(1,2,3)
                .flatMap(ii -> Observable.just(ii))
                .subscribe(ts);

        assertThat(ts.getOnNextEvents(), equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void testCombineLists() throws Exception {
        Observable.just(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6)
        ).flatMap(ii -> Observable.from(ii))
                .subscribe(ts);

        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);
        assertThat(ts.getOnNextEvents(), equalTo(expected));
    }

    @Test
    public void testIterable() throws Exception {
        Observable.just(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6)
        ).flatMapIterable(ii -> ii) // don't have to make the observable
                .subscribe(ts);

        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);
        assertThat(ts.getOnNextEvents(), equalTo(expected));
    }

    @Test
    public void testKeepOriginal() throws Exception {
        class Tuple {
            List<Integer> list;
            Integer item;

            public Tuple(List<Integer> list, Integer item) {
                this.list = list;
                this.item = item;
            }
        }

        Observable.just(
                Arrays.asList(1),
                Arrays.asList(2 ,3)
        ).flatMap(ii -> Observable.from(ii),
                (list, item) -> new Tuple(list, item))
                .subscribe(ts);

        Tuple t1 = ((Tuple) ts.getOnNextEvents().get(0));
        Tuple t2 = ((Tuple) ts.getOnNextEvents().get(1));
        Tuple t3 = ((Tuple) ts.getOnNextEvents().get(2));

        assertThat(t1.item, equalTo(1));
        assertThat(t1.list, equalTo(Arrays.asList(1)));
        assertThat(t2.item, equalTo(2));
        assertThat(t2.list, equalTo(Arrays.asList(2, 3)));
        assertThat(t3.item, equalTo(3));
        assertThat(t3.list, equalTo(Arrays.asList(2, 3)));
    }

    @Test
    public void testWithErrorAndCompletedFunctions() throws Exception {
        PublishSubject<String> subject = PublishSubject.create();
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        subject.flatMap(new Func1<String, Observable<String>>() {
            @Override
            public Observable<String> call(String s) {
                return Observable.just("next:" + s);
            }
        }, new Func1<Throwable, Observable<String>>() {
            @Override
            public Observable<String> call(Throwable throwable) {
                return Observable.just(throwable.getMessage());
            }
        }, new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                return Observable.just("complete");
            }
        }).subscribe(testSubscriber);

        subject.onNext("a");
        subject.onNext("b");
        subject.onError(new RuntimeException("bad"));
//        subject.onCompleted();

        assertThat(testSubscriber.getOnNextEvents(), contains("next:a", "next:b", "bad"));
    }

    @Test
    public void testWtfWontCompile() throws Exception {
//        PublishSubject<String> subject = PublishSubject.create();
//        TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        // Neither of the two following clumps compiles with lambdas, you must use
        // inner classes instead :(
//        subject.flatMap(s -> {
//            return Observable.just("next:" + s);
//        }, throwable -> {
//            return Observable.just("error");
//        }, () -> {
//            return Observable.just("complete");
//        }).subscribe(testSubscriber);

//        subject.flatMap(ii -> Observable.just("hi"),
//                        (error) -> Observable.just("error"),
//                        () -> Observable.just("complete"))

//        subject.onNext("a");
//        subject.onNext("b");
//        subject.onError(new RuntimeException("bad"));
//        subject.onCompleted();
//
//        assertThat(testSubscriber.getOnNextEvents(), contains("next:a", "next:b", "error"));
    }
}


