package edu.brown.cs.bigdata.chsanfor.AudioEMD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Clayton on 1/15/18.
 */
public class MatchingGroups {
    public static List<Integer> MatchGroups(Integer[] data1, Integer[] data2, int clusterSize1, int clusterSize2) {
        List<Integer> list1 = Arrays.asList(data1);
        List<Integer> list2 = Arrays.asList(data2);
        java.util.Collections.shuffle(list1);
        java.util.Collections.shuffle(list2);
        System.out.println(list1.toString());
        System.out.println(list2.toString());
        int index1 = 0;
        int index2 = 0;
        List<Integer> outList = new ArrayList<>();
        while (index1 < list1.size() && index2 < list2.size()) {
            List<Integer> sublist1 = new ArrayList<>();
            for (int i = 0; i < clusterSize1; i++) {
                if (index1 < list1.size()) sublist1.add(list1.get(index1));
                index1++;
            }
            List<Integer> sublist2 = new ArrayList<>();
            for (int j = 0; j < clusterSize2; j++) {
                if (index2 < list2.size()) sublist2.add(list2.get(index2));
                index2++;
            }
            outList.addAll(bipartiteProd(sublist1, sublist2));
        }
        System.out.println(outList.toString());
        return outList;
    }

    public static List<Integer> bipartiteProd(List<Integer> sublist1, List<Integer> sublist2) {
        List<Integer> output = new ArrayList<>();
        for (Integer elt1 : sublist1) {
            for (Integer elt2 : sublist2) {
                output.add(elt1 * elt2);
            }
        }
        System.out.println(output.toString());
        return output;
    }
}
