package com.github.chenmingang.snowflake.paxos.simplify;

import com.github.chenmingang.snowflake.paxos.Meta;

import java.util.ArrayList;
import java.util.List;

public class Node {

    //test
    static List<Acceptor> acceptors = new ArrayList<>();
    static List<Learner> learners = new ArrayList<>();

    public static void main(String[] args) {
        Node node1 = new Node();
        Node node2 = new Node();
        Node node3 = new Node();

        Proposer proposer1 = node1.startNode("a");
        Proposer proposer2 = node2.startNode("b");
        Proposer proposer3 = node3.startNode("c");

        doTest(proposer1);
        doTest(proposer2);
        doTest(proposer3);

        Meta.INSTANCE.print();

    }

    private static void doTest(Proposer proposer) {
        while (true) {
            long prepare = proposer.prepare();
            long notAcceptNum = acceptors.stream().map(acceptor -> acceptor.handlePrepare(prepare))
                    .filter(i -> !i).count();
            if (notAcceptNum <= 0) {
                break;
            }
        }
        long[] result = new long[1];
        while (true) {
            long proposerNum = proposer.proposer();
            long acceptNum = acceptors.stream().map(acceptor -> acceptor.handleProposer(proposer.getName(), proposerNum))
                    .filter(i -> i).count();
            if (acceptNum > acceptors.size() / 2) {
                result[0] = proposerNum;
                break;
            }
        }

        learners.forEach(learner -> {
            learner.learn(proposer.getName(), result[0]);
        });
    }

    public Proposer startNode(String name) {
        Acceptor local = new Acceptor();
        acceptors.add(local);
        Proposer proposer = new Proposer(name);
        learners.add(new Learner());
        return proposer;
    }

}
