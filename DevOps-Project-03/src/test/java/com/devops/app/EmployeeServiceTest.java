package com.devops.app;

import com.devops.app.model.Employee;
import com.devops.app.repository.EmployeeRepository;
import com.devops.app.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees() {
        Employee e1 = new Employee("Alice", "alice@test.com", "Engineering", "Dev");
        Employee e2 = new Employee("Bob", "bob@test.com", "DevOps", "Engineer");
        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<Employee> result = service.getAllEmployees();
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetEmployeeById() {
        Employee emp = new Employee("Alice", "alice@test.com", "Engineering", "Dev");
        emp.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(emp));

        Optional<Employee> result = service.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    void testCreateEmployee() {
        Employee emp = new Employee("Charlie", "charlie@test.com", "QA", "Tester");
        when(repository.save(emp)).thenReturn(emp);

        Employee created = service.createEmployee(emp);
        assertNotNull(created);
        assertEquals("Charlie", created.getName());
    }

    @Test
    void testDeleteEmployee() {
        doNothing().when(repository).deleteById(1L);
        service.deleteEmployee(1L);
        verify(repository, times(1)).deleteById(1L);
    }
}
