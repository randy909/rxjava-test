import org.junit.Test;

import rx.Observable;
import rx.observables.ConnectableObservable;

public class FizzBuzzTest {

    // Simplest solution but hardly exercises any rx coolness
    @Test
    public void testMap() throws Exception {
        Observable.range(1, 100)
                .map(num -> {
                    String answer = "";
                    if (num % 3 == 0) {
                        answer += "Fizz";
                    }
                    if (num % 5 == 0) {
                        answer += "Buzz";
                    }
                    if (answer.isEmpty())
                        return num;
                    return answer;
                })
                .subscribe(System.out::println);
    }

    @Test
    public void testZipWith() throws Exception {
        ConnectableObservable<Integer> observable = Observable.range(1, 100).publish();
        Observable<String> fizz = observable.map(num -> num % 3 == 0 ? "Fizz" : "");
        Observable<String> buzz = observable.map(num -> num % 5 == 0 ? "Buzz" : "");
        Observable<String> nums = observable
                .map(num -> (num % 3 != 0 && num % 5 != 0) ? String.valueOf(num) : "");

        fizz.zipWith(buzz, (f, b) -> f + b)
            .zipWith(nums, (fb, n) -> fb + n)
            .subscribe(System.out::println);

        observable.connect();
    }

    // Works without proper newlines
    @Test
    public void testFilter() throws Exception {
        ConnectableObservable<Integer> observable = Observable.range(1, 100).publish();
        observable.filter(num -> num % 3 == 0)
                .subscribe(num -> System.out.print("Fizz"));
        observable.filter(num -> num % 5 == 0)
                .subscribe(num -> System.out.print("Buzz"));
        observable.filter(num -> num % 3 != 0 && num % 5 != 0)
                .subscribe(num -> System.out.print(num));

        observable.connect();
    }

    // Works without proper newlines
    @Test
    public void testMerge() throws Exception {
        ConnectableObservable<Integer> observable = Observable.range(1, 100).publish();
        Observable<String> fizz = observable.filter(num -> num % 3 == 0).map(num -> "Fizz");
        Observable<String> buzz = observable.filter(num -> num % 5 == 0).map(num -> "Buzz");
        Observable<String> nums = observable.filter(num -> num % 3 != 0 && num % 5 != 0).map(num -> String.valueOf(num));

        fizz.mergeWith(buzz).mergeWith(nums).subscribe(System.out::print);

        observable.connect();
    }

    // Does not work, println list of "FizzBuzzXX"
    @Test
    public void testCombineLatest() throws Exception {
        ConnectableObservable<Integer> observable = Observable.range(1, 100).publish();
        Observable<String> fizz = observable.filter(num -> num % 3 == 0).map(num -> "Fizz");
        Observable<String> buzz = observable.filter(num -> num % 5 == 0).map(num -> "Buzz");
        Observable<String> nums = observable.filter(num -> num % 3 != 0 && num % 5 != 0).map(num -> String.valueOf(num));

        fizz.combineLatest(fizz, buzz, nums, (f, b, n) -> f + b + n).subscribe(System.out::println);

        observable.connect();
    }
}
