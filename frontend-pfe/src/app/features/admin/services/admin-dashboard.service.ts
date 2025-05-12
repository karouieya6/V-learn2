import { HttpClient,HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private baseUrl = 'http://localhost:8080/userservice/dashboard/admin';

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
  deleteStudent(userId: number) {
    const headers = this.getAuthHeaders();
    return this.http.delete(`${this.baseUrl}/students/${userId}`, { headers });
  }
  deleteInstructorst(userId: number) {
    const headers = this.getAuthHeaders();
    return this.http.delete(`${this.baseUrl}/instructors/${userId}`, { headers });
  }
  getStudents(search: string = '', page: number = 0, size: number = 10) {
    const headers = this.getAuthHeaders();
  
    const params = {
      search,
      page: page.toString(),
      size: size.toString()
    };
  
    return this.http.get<any>(`${this.baseUrl}/students`, { headers, params });
  }
  
  getInstructors(search: string = '', page: number = 0, size: number = 10) {
    const headers = this.getAuthHeaders();
  
    const params = {
      search,
      page: page.toString(),
      size: size.toString()
    };
  
    return this.http.get<any>(`${this.baseUrl}/instructors`, { headers, params });
  }
  getInstructorRequests(search: string = '', page: number = 0, size: number = 10) {
    const headers = this.getAuthHeaders();
    const params = {
      search,
      page: page.toString(),
      size: size.toString()
    };
    return this.http.get<any>('http://localhost:8080/userservice/dashboard/admin/instructor-requests',{ headers, params });
  }
  acceptInstructorRequest(userId: number) {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.baseUrl}/approve-instructor/${userId}`,{}, { headers });
  }
  rejectInstructorRequest(userId: number) {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.baseUrl}/reject-instructor/${userId}`,{}, { headers });
  }
  getAdminDasbord() {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.baseUrl}/overview`, { headers});
  }
}
