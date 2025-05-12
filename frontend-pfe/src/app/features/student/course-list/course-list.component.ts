import { Component, OnInit } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
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
  getToken(): string | null {
    return localStorage.getItem('token');
  }
  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }
  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user?.id;
    const headers = this.getAuthHeaders();
    this.http.get<any>(`http://localhost:8080/enrollmentservice/api/enrollments/user/${userId}`, { headers }).subscribe({
      next: (courses) => {
        console.log("courses",courses.data);
        
        this.courseList = courses.data.content;
      },
      error: () => {
        console.error('Failed to load enrolled courses.');
      },
    });
  }
}
