import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { InstructorDashboardService } from '../services/instructor-dashboard.service';

@Component({
  selector: 'app-instructor-course-list',
  standalone: true,
  templateUrl: './instructor-course-list.component.html',
  imports: [CommonModule, FormsModule, NgbPaginationModule],
})
export class InstructorCourseListComponent implements OnInit {
  courses: any[] = [];
  searchTerm = '';
  sortBy = 'newest';
  page = 1;
  pageSize = 10;
  totalCourses = 0;
  totalPages = 0;

  constructor(private instructorService: InstructorDashboardService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses() {
    this.instructorService.getInstructorCourses(this.searchTerm, this.sortBy, this.page - 1, this.pageSize).subscribe({
      next: (res) => {
        this.courses = res.courses;
        this.totalCourses = res.total;
        this.totalPages = Math.ceil(this.totalCourses / this.pageSize);
      },
      error: (err) => console.error('Failed to load courses', err),
    });
  }

  onSearchChange() {
    this.page = 1;
    this.loadCourses();
  }

  onSortChange() {
    this.page = 1;
    this.loadCourses();
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.loadCourses();
  }
}
