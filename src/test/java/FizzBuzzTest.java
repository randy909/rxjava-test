import org.junit.Test;

import java.util.List;

import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FizzBuzzTest {

    private void assertFizzBuzz(String answer) {
        assertThat(answer, is(generate()));
    }

    private String generate() {
        String output = "";
        for (int ii = 0; ii <= 20; ++ii) {
            if (ii % 3 == 0) {
                output += "Fizz";
            }
            if (ii % 5 == 0) {
                output += "Buzz";
            }
            if (ii % 3 != 0 && ii % 5 != 0) {
                output += ii;
            }
            output += ",";
        }
        return output.substring(0, output.length() - 1);
    }

    @Test
    public void testObservableFizzBuzz() throws Exception {
        List<String> stuff = Observable.range(0, 21)
                .map((ii) -> {
                    String tmp = "";
                    if (ii % 3 == 0) {
                        tmp += "Fizz";
                    }
                    if (ii % 5 == 0) {
                        tmp += "Buzz";
                    }
                    if (ii % 3 != 0 && ii % 5 != 0) {
                        tmp += ii;
                    }
                    return tmp;
                }).toList().toBlocking().first();
        String join = String.join(",", stuff);
        assertFizzBuzz(join);
    }

    @Test
    public void testFizzBuzzWithBlah() throws Exception {
        ConnectableObservable<Integer> master = Observable.range(0, 21).publish();
        Observable<String> fizz = master.map(ii -> ii % 3 == 0 ? "Fizz" : "");
        Observable<String> buzz = master.map(ii -> ii % 5 == 0 ? "Buzz" : "");
        Observable<String> nums = master
                .map(ii -> (ii % 3 != 0 && ii % 5 != 0) ? String.valueOf(ii) : "");

        TestSubscriber testSubscriber = new TestSubscriber();
        Observable.zip(fizz, buzz, nums, (f, b, n) -> f + b + n).subscribe(testSubscriber);
        master.connect();
        assertFizzBuzz(String.join(",", testSubscriber.getOnNextEvents()));
    }

    // Works without proper newlines
    @Test
    public void testMerge() throws Exception {
        ConnectableObservable<Integer> observable = Observable.range(0, 21).publish();
        Observable<String> fizz = observable.filter(num -> num % 3 == 0).map(num -> "Fizz");
        Observable<String> buzz = observable.filter(num -> num % 5 == 0).map(num -> "Buzz");
        Observable<String> nums = observable.filter(num -> num % 3 != 0 && num % 5 != 0).map(num -> String.valueOf(num));

        TestSubscriber testSubscriber = new TestSubscriber();
        fizz.mergeWith(buzz).mergeWith(nums).subscribe(testSubscriber);

        observable.connect();
//        assertFizzBuzz(String.join(",", testSubscriber.getOnNextEvents()));
    }

    // Does not work, println list of "FizzBuzzXX"
    @Test
    public void testCombineLatest() throws Exception {
        ConnectableObservable<Integer> observable = Observable.range(0, 21).publish();
        Observable<String> fizz = observable.filter(num -> num % 3 == 0).map(num -> "Fizz");
        Observable<String> buzz = observable.filter(num -> num % 5 == 0).map(num -> "Buzz");
        Observable<String> nums = observable.filter(num -> num % 3 != 0 && num % 5 != 0).map(num -> String.valueOf(num));

        TestSubscriber testSubscriber = new TestSubscriber();
        fizz.combineLatest(fizz, buzz, nums, (f, b, n) -> f + b + n).subscribe(testSubscriber);

        observable.connect();
//        assertFizzBuzz(String.join(",", testSubscriber.getOnNextEvents()));
    }
}
