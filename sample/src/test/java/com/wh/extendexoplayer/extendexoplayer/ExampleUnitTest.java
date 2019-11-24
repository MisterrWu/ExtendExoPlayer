package com.wh.extendexoplayer.extendexoplayer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        int[] arr = {1, 2, 4, 5, 3, 4, 6, 1};
        bubbleSort(arr);
        print(arr);
    }

    private void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) { // 8,7,6,5,4,3,2,1,0
            // 1(7-0)
            // 2(6-0)
            // 3(5-0)
            // .
            // .
            // .
            for (int j = 0; j < (arr.length - i) - 1; j++) { // 每趟循环后都有一个最大值排到后面，减一是因为最后一个值没有比较了。
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    private void print(int[] arr) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]).append((i != arr.length - 1) ? ", " : "");
        }
        builder.append("]");
        System.out.println(builder.toString());
    }
}