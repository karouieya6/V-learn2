import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userServiceUrl = 'http://localhost:8081/auth'; // for login
  private enrollmentServiceUrl = 'http://localhost:8080/api/enrollments'; // for enrolled courses

  constructor(private http: HttpClient) {}

  login(userData: { email: string; password: string }): Observable<any> {
    return this.http.post(`${this.userServiceUrl}/login`, userData);
  }

  getEnrolledCourses(userId: number): Observable<any[]> {
    const token = localStorage.getItem('token');

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<any[]>(`${this.enrollmentServiceUrl}/user/${userId}`, { headers });
  }
}
