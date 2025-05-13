import { HttpClient,HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}
  
  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }
  getToken(): string | null {
    return localStorage.getItem('token');
  }
  
  getTotalCourses() {
    const headers = this.getAuthHeaders();
     const res=this.http.get<number>('http://localhost:8080/courseservice/api/courses/count')
 console.log("res",res);
 return res;
 
    }

  getTotalEnrollments(userId: number) {
    const headers = this.getAuthHeaders();
    return this.http.get<number>(`http://localhost:8080/enrollmentservice/api/enrollments/user/${userId}/count`, { headers });
  }

  getCompletedCourses(userId: number) {
    const headers = this.getAuthHeaders();
    return this.http.get<number>(`http://localhost:8080/certificateservice/api/certificates/user/${userId}/count`, { headers });
  }
}
