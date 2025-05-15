import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
// keep this
import {jwtDecode} from 'jwt-decode';
@Component({
  selector: 'app-sign-in-area',
  standalone: true,
  templateUrl: './sign-in-area.component.html',
  styleUrls: ['./sign-in-area.component.scss'],
  imports: [
    RouterModule,
    FormsModule
  ],
})
export class SignInAreaComponent implements OnInit {

  loginData = {
    email: '',
    password: ''
  };
  ngOnInit(): void {
    // you can leave it empty for now
  }
  public hideHeader: boolean = true;

  constructor(
    private http: HttpClient,
    private router: Router,
 // inject here!
  ) {}



  onLogin() {
    const payload = {
      email: this.loginData.email.trim(),
      password: this.loginData.password
    };
  
    this.http.post('http://localhost:8080/userservice/auth/login', payload, {
      headers: { 'Content-Type': 'application/json' }
    }).subscribe({
      next: (res: any) => {
          localStorage.setItem('token', res.token);
  
        const decodedToken: any = jwtDecode(res.token);
        localStorage.setItem('user', JSON.stringify(decodedToken));
        const roles: string[] = decodedToken.roles;
  
        alert('Welcome back!');
      
        if (roles.includes('ADMIN')) {
      this.router.navigate(['/admin/dashboard'])
  .then(() => {
    console.log('✅ Navigated to admin dashboard');
  })
  .catch((err) => {
    console.error('❌ Navigation to admin dashboard failed:', err);
  });        } else if (roles.includes('INSTRUCTOR')) {
          this.router.navigate(['/instructor/dashboard']);
        } else {
          this.router.navigate(['/student/dashboard']);
        }
      },
      error: (err) => {
        console.error('❌ Login failed:', err);
        alert('Invalid email or password.');
      }
    });
  }
}