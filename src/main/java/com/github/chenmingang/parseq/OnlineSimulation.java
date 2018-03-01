package com.github.chenmingang.parseq;

import com.linkedin.parseq.ParTask;
import com.linkedin.parseq.Task;
import com.linkedin.parseq.Tasks;

import java.util.ArrayList;
import java.util.List;

public class OnlineSimulation {
//    static EngineAgent engine = EngineFactory.defaultEngine();
    static EngineAgent engine = EngineFactory.getEngine(20,1,20);

    public static void main(String[] args) {
        long l1 = System.currentTimeMillis();
        List<Model> result1 = new OnlineSimulation().queryAsync();
        long l2 = System.currentTimeMillis();
        System.out.println("time1:" + (l2 - l1));
        //time1:1063

        long l3 = System.currentTimeMillis();
        List<Model> result2 = new OnlineSimulation().query();
        long l4 = System.currentTimeMillis();
        System.out.println("time2:" + (l4 - l3));
        //time2:20060

        engine.shutdown();
    }

    //n+1 查询
    public List<Model> query() {
        // 第一步查询
        List<Model> modelList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            modelList.add(new Model(i));
        }
        // 第二步查询
        for (Model model : modelList) {
            setName(model);
        }
        return modelList;
    }

    //n+1 查询
    public List<Model> queryAsync() {

        // 第一步查询
        List<Model> modelList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            modelList.add(new Model(i));
        }
        // 第二步查询
        List<Task<Model>> tasks = new ArrayList<>();
        for (Model model : modelList) {
            Task<Model> task = engine.task(model, (Model i) -> setName(i));
            tasks.add(task);
        }
        ParTask<Model> parTask = Tasks.par(tasks);
        engine.run(parTask);
        try {
            parTask.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return modelList;
    }

    /**
     * 执行子查询并合并数据的模拟
     * @param model
     * @return
     */
    private Model setName(Model model) {
        model.setName("name-" + model.getId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return model;
    }

    class Model {
        private Integer id;
        private String name;

        public Model(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
