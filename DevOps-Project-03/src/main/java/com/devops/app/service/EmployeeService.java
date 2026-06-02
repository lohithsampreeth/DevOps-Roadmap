package com.devops.app.service;

import com.devops.app.model.Employee;
import com.devops.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return repository.findById(id);
    }

    public Employee createEmployee(Employee employee) {
        return repository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        return repository.findById(id).map(emp -> {
            emp.setName(updated.getName());
            emp.setEmail(updated.getEmail());
            emp.setDepartment(updated.getDepartment());
            emp.setRole(updated.getRole());
            return repository.save(emp);
        }).orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }

    public void deleteEmployee(Long id) {
        repository.deleteById(id);
    }

    public List<Employee> getByDepartment(String department) {
        return repository.findByDepartment(department);
    }
}
