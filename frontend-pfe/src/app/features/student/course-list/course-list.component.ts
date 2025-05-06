import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgbPaginationModule, NgbProgressbarModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'dashboard-course-list',
  standalone: true,
  imports: [NgbPaginationModule, NgbProgressbarModule],
  templateUrl: './course-list.component.html',
})
export class CourseListComponent implements OnInit {
  courseList: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user?.id;

    this.http.get<any[]>(`http://localhost:8080/enrollmentservice/enrollments/user/${userId}`).subscribe({
      next: (courses) => {
        this.courseList = courses;
      },
      error: () => {
        console.error('Failed to load enrolled courses.');
      },
    });
  }
}
