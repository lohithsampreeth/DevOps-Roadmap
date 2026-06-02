package com.devops.app.controller;

import com.devops.app.model.Task;
import com.devops.app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ── UI Endpoints ──────────────────────────────────────
    @GetMapping("/")
    public String dashboard(Model model) {
        List<Task> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("newTask", new Task());
        model.addAttribute("totalTasks", tasks.size());
        model.addAttribute("todoCount",
            tasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count());
        model.addAttribute("inProgressCount",
            tasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count());
        model.addAttribute("doneCount",
            tasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count());
        return "index";
    }

    @PostMapping("/tasks")
    public String createTask(@ModelAttribute Task task) {
        taskService.createTask(task);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam Task.Status status) {
        Task task = taskService.getTaskById(id);
        task.setStatus(status);
        taskService.updateTask(id, task);
        return "redirect:/";
    }

    // ── REST API Endpoints ────────────────────────────────
    @GetMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<List<Task>> apiGetAll() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/api/tasks/{id}")
    @ResponseBody
    public ResponseEntity<Task> apiGetOne(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<Task> apiCreate(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @PutMapping("/api/tasks/{id}")
    @ResponseBody
    public ResponseEntity<Task> apiUpdate(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseBody
    public ResponseEntity<Void> apiDelete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // Health check endpoint
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }
}
