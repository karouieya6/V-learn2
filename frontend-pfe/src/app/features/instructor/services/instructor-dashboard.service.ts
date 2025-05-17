import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InstructorDashboardService {
  private baseUrl = 'http://localhost:8080/userservice/dashboard/instructor';
  constructor(private http: HttpClient) { }
   private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  getOverview(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.baseUrl}/overview`, { headers });
  }
  getInstructorCourses(search = '', sortBy = 'newest', page = 0, size = 10) {
 const headers = this.getAuthHeaders();
  const params = { search, sortBy, page, size };
    return this.http.get<any>(`${this.baseUrl}/courses`, { headers, params });
}

}
