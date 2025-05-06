import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  getTotalCourses() {
    return this.http.get<number>('http://localhost:8080/courseservice/courses/count');
  }

  getTotalEnrollments(userId: number) {
    return this.http.get<number>(`http://localhost:8080/enrollmentservice/enrollments/user/${userId}/count`);
  }

  getCompletedCourses(userId: number) {
    return this.http.get<number>(`http://localhost:8080/certificateservice/certificates/user/${userId}/count`);
  }
}
