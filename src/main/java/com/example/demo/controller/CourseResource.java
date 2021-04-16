package com.example.demo.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.dao.CourseJPA;
import com.example.demo.model.Course;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200", "http://localhost:8081" })
@RestController
public class CourseResource {
	@Autowired
	CourseJPA jpa;

//	@Autowired
//	private CoursesHardcodedService courseManagementService;

	@GetMapping("/instructors/{username}/courses")
	public List<Course> getAllCourses(@PathVariable String username) {
		return jpa.findAll();
	}

	@DeleteMapping("/instructors/{username}/courses/{id}")
	public List<Course>  deleteCourse(
			@PathVariable String username, 
			@PathVariable long id) {

		jpa.deleteById(id);

//		if (course != null) {
//			return ResponseEntity.noContent().build();
//		}

		return jpa.findAll();
	}

	@GetMapping("/instructors/{username}/courses/{id}")
	public Optional<Course> getCourse(@PathVariable String username, @PathVariable long id) {
		return jpa.findById(id);
	}

	@PutMapping("/instructors/{username}/courses/{id}")
	public ResponseEntity<Course> updateCourse(@PathVariable String username, @PathVariable long id,
			@RequestBody Course course) {

		Course courseUpdated = jpa.save(course);
		return new ResponseEntity<Course>(courseUpdated, HttpStatus.OK);
	}

	@PostMapping("/instructors/{username}/courses")
	public ResponseEntity<Void> createCourse(@PathVariable String username, @RequestBody Course course) {

		Course createdCourse = jpa.save(course);

		// Location
		// Get current resource url
		/// {id}
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdCourse.getId())
				.toUri();

		return ResponseEntity.created(uri).build();
	}

}
