package lids.ad.wuliang.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MatrixTest {
    private static int[][] ARRAYS;
    private static Matrix mtx;
    private static void printArray(int[] arr){
        int i = 0;
        while (i < arr.length){
            System.out.print(arr[i] + ",");
            i++;
        }
        System.out.println();
    }
    private static int[] randomArray(int size) {
        Random random = new Random();
        int[] data = new int[size];
        int i = 0;
        while(i < size) {
            data[i] = random.nextInt(1<<30);
            i++;
        }
        return data;
    }

    @BeforeEach
    void setUp() {
        ARRAYS = new int[][] {
                new int[]{1,2,3,4},
                new int[]{5,6,7,8},
                randomArray(1),
                randomArray(10),
                randomArray(100),
                randomArray(1000),
                randomArray(10_000),
                randomArray(100_000),
                randomArray(1000_000),
                randomArray(10_000_000)
        };

        mtx = new Matrix();
        for(int i=0; i <ARRAYS.length; i++) {
            for (int j = 0; j < ARRAYS[i].length; j++) {
                mtx.add(i, ARRAYS[i][j]);
            }
        }
    }

    @AfterEach
    void tearDown() {
        mtx.clear();
    }

    @Test
    void updateColumn() {
        Random random = new Random();
        for (int i=0; i < 10; i++) {
            int columnIndex = random.nextInt(1 << 30);
            HashSet<Integer> st = mtx.getColumn(columnIndex);
            System.out.println("column: " + columnIndex + " , value: " + st.toString());
            //int[] arr = new int[]{0, 2, 4, 6, 8};
            HashSet<Integer> arr = new HashSet<>(Arrays.asList(0, 2, 4, 6, 8));
            mtx.updateColumn(columnIndex, new HashSet<Integer>(arr));
            st = mtx.getColumn(columnIndex);
            assertEquals("[0, 2, 4, 6, 8]", st.toString());
            //int[] arr1 = new int[]{1, 3, 5, 7, 9, 11};
            HashSet<Integer> arr1 = new HashSet<>(Arrays.asList(1, 3, 5, 7, 9, 11));
            mtx.updateColumn(columnIndex, arr1);
            st = mtx.getColumn(columnIndex);
            assertEquals("[1, 3, 5, 7, 9, 11]", st.toString());
        }
    }

    @Test
    void getRow() {
        int i = 0;
        for (; i<ARRAYS.length;i++){
            RoaringBitmap rb = mtx.getRow(i);
            assertEquals(rb.isEmpty(),false);
            for (int j = 0; j < ARRAYS[i].length; j++) {
                assertEquals(rb.contains(ARRAYS[i][j]),true);
            }
            rb.clear();
        }
        assertEquals(mtx.getRow(i).isEmpty(),true);
    }

    @Test
    void unionRow() {
        ArrayList<Integer> arr = new ArrayList<Integer>(Arrays.asList(0,1));
        RoaringBitmap rb = mtx.unionRows(arr);
        System.out.println(rb.toString());
        assertEquals("{1,2,3,4,5,6,7,8}", rb.toString());

        int loop = 0;
        Random random = new Random();
        while(loop < 3){
            int unionNumber = random.nextInt(10);
            System.out.println(unionNumber);
            //int[] data = new int[unionNumber];
            ArrayList<Integer> data = new ArrayList<>();
            for(int i=0; i<unionNumber; i++){
                //data[i] = (random.nextInt(1<<30)) % 10;
                data.add((random.nextInt(1<<30)) % 10);
            }
            rb.clear();
            rb = mtx.unionRows(data);
            for(int i=0; i <unionNumber; i++) {
                for (int j = 0; j < ARRAYS[data.get(i)].length; j++) {
                    assertEquals(true,rb.contains(ARRAYS[data.get(i)][j]));
                }
            }
            loop++;
        }
    }

    @Test
    void testAdd() {
        Matrix mtx = new Matrix();
        RoaringBitmap rb = mtx.getRow(1);
        assertEquals(true,rb.isEmpty());
        mtx.add(1,1);
        rb = mtx.getRow(1);
        assertEquals(false,rb.isEmpty());
        assertEquals("{1}",rb.toString());
    }

    @Test
    void testToString() {
        Matrix mtx = new Matrix();
        int[][] arr = new int[][]{
                new int[]{1, 2, 3, 4},
                new int[]{5, 6, 7, 8}
        };
        for (int i=0; i< arr.length; i++) {
            for(int j=0; j< arr[i].length; j++) {
                mtx.add(i,arr[i][j]);
            }
        }
        System.out.println(mtx.toString());
        assertEquals("{0={1,2,3,4}, 1={5,6,7,8}}",mtx.toString());
        mtx.clear();
        System.out.println(mtx.toString());
        assertEquals("{}",mtx.toString());
    }

    @Test
    void testToString1() {
        Matrix mtx = new Matrix();
        int[][] arr = new int[][]{
                new int[]{1, 2, 3, 4},
                new int[]{2, 3, 4, 5}
        };
        for (int i=0; i< arr.length; i++) {
            for(int j=0; j< arr[i].length; j++) {
                mtx.add(i,arr[i][j]);
            }
        }
        assertEquals("{0}",mtx.toString(1));
        assertEquals("{0,1}",mtx.toString(2));
        assertEquals("{0,1}",mtx.toString(3));
        assertEquals("{0,1}",mtx.toString(4));
        assertEquals("{1}",mtx.toString(5));
        assertEquals("{}",mtx.toString(6));
    }
}