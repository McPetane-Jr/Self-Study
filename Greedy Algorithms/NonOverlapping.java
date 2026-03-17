import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;



class Result {

    /*
     * Complete the 'maximizeNonOverlappingMeetings' function below.
     *
     * The function is expected to return an INTEGER.
     * The function accepts 2D_INTEGER_ARRAY meetings as parameter.
     */

    public static int maximizeNonOverlappingMeetings(List<List<Integer>> meetings) {
    // Write your code here
    int n = meetings.size();
    if (n<2) return 1;
    
    //sort
    meetings.sort((a, b) -> {
        return a.get(0) - b.get(0);
    });
    
    
    int count = 0;
    int leftEnd = meetings.get(0).get(1);
    
    for (int i= 1; i < n; i++){
        
        int rightStart = meetings.get(i).get(0);
        int rightEnd = meetings.get(i).get(1);

        //test if they are'nt overlapping
        if ( rightStart >= leftEnd){
            
            //set prevEnd to the current end value
            leftEnd = rightEnd;
            
        }
        else {
            count++;
            leftEnd = Math.min(rightEnd, leftEnd);
        }
        
    }
    
    return n -count;
    }

}

public class NonOverlapping {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        int meetingsRows = Integer.parseInt(bufferedReader.readLine().trim());
        int meetingsColumns = Integer.parseInt(bufferedReader.readLine().trim());

        List<List<Integer>> meetings = new ArrayList<>();

        IntStream.range(0, meetingsRows).forEach(i -> {
            try {
                meetings.add(
                    Stream.of(bufferedReader.readLine().replaceAll("\\s+$", "").split(" "))
                        .map(Integer::parseInt)
                        .collect(toList())
                );
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        int result = Result.maximizeNonOverlappingMeetings(meetings);

        System.out.println(result);

        bufferedReader.close();
    }
}
