import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-sign-up-area',
  standalone: true,
  templateUrl: './sign-up-area.component.html',
  styleUrls: ['./sign-up-area.component.scss'],
  imports: [
    RouterModule,
    FormsModule
  ],
})
export class SignUpAreaComponent implements OnInit {

  registerData = {
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  };
  public hideHeader: boolean = true;

  constructor(private http: HttpClient , private router: Router) {}

  ngOnInit()
  : void {
    this.router.events.subscribe(() => {
      const currentRoute = this.router.url;
      this.hideHeader = currentRoute.includes('/sign-in') || currentRoute.includes('/sign-up');
    });
  }

  onSubmit() {
    // 1. Check if passwords match
    if (this.registerData.password !== this.registerData.confirmPassword) {
      alert('Passwords do not match!');
      return;
    }
  
    // 2. Clean payload (remove confirmPassword + trim + fix optional fields)
    const payload = {
      username: this.registerData.username?.trim(),
      email: this.registerData.email?.trim(),
      password: this.registerData.password,
      firstName: this.registerData.firstName?.trim() || null,
      lastName: this.registerData.lastName?.trim() || null,
      phone: this.registerData.phone?.trim() || null
    };
  
    console.log('✅ Payload being sent:', payload);
    
    // 3. Send request with correct Content-Type
    this.http.post('http://localhost:8080/userservice/auth/register', payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).subscribe({
      next: (response) => {
        console.log('✅ Registration successful:', response);
        alert('Account created successfully!');
        this.router.navigate(['/sign-in']);
      },
      error: (error) => {
        console.error('❌ Registration error:', error);
        alert('Something went wrong. Please check your input and try again.');
      }
    });
  }
}  