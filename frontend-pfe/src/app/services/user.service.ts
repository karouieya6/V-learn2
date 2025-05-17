import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';

import { catchError, map, tap } from 'rxjs/operators';
@Injectable({
  providedIn: 'root',
})
export class UserService {
  private baseUrl = 'http://localhost:8080/userservice/auth'; 
  private userApiUrl = 'http://localhost:8080/userservice/user';

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials).pipe(
      tap((response: any) => {
        if (response.token) {
          this.saveToken(response.token);
          // Optionally save user data
          localStorage.setItem('user', JSON.stringify(response.user));
        }
      }),
      catchError(this.handleError('login'))
    );
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, data).pipe(
      catchError(this.handleError('register'))
    );
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): Observable<boolean> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.baseUrl}/logout`, {}, { headers }).pipe(
      map(() => true), 
      tap(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.router.navigate(['/sign-in']);
      }),
      catchError((error) => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.router.navigate(['/sign-in']);
        return of(true);
      })
    );
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getEnrolledCourses(userId: number): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.baseUrl}/api/enrollments/user/${userId}`, { headers }).pipe(
      catchError(this.handleError<any[]>('getEnrolledCourses', []))
    );
  }

  getProfile(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.userApiUrl}/profile`, { headers }).pipe(
      tap((user) => {
        localStorage.setItem('user', JSON.stringify(user));
      }),
      catchError(this.handleError('getProfile'))
    );
  }

  updateProfile(data: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.userApiUrl}/profile`, data, { headers }).pipe(
      tap((updatedUser) => {
        localStorage.setItem('user', JSON.stringify(updatedUser));
      }),
      catchError(this.handleError('updateProfile'))
    );
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }
  changePassword(data: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.userApiUrl}/change-password`, data, {
      headers,
      responseType: 'text' 
    }).pipe(
      catchError(this.handleError('changePassword'))
    );
  }
  
  // Error handling utility
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      throw error; // ⛔️ don't use `of(result as T)` here, or it will go to `.next()`
    };
  }
  
  uploadProfilePicture(userId: number, formData: FormData): Observable<string> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${this.getToken()}`
    });
  
    return this.http.post(
      `http://localhost:8080/userservice/user/${userId}/upload-profile`,
      formData,
      { headers, responseType: 'text' } // 👈 FIX: explicitly set responseType to 'text'
    ).pipe(
      tap((imageUrl) => {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        user.imageUrl = imageUrl;
        localStorage.setItem('user', JSON.stringify(user));
      }),
      catchError(this.handleError<string>('uploadProfilePicture'))
    );
  }

  getProfileImageUrl(imagePath: string | null): string | null {
    if (!imagePath) return null;
      const headers = this.getAuthHeaders();
    const filename = imagePath.split('/').pop(); 
    console.log("filename",filename);
    
    return `http://localhost:8080/userservice/files/profile/${filename}`;
  }
  
  
}