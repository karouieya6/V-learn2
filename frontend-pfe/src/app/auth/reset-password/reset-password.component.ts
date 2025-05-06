import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-reset-password',
  standalone:true,
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
  imports: [CommonModule, FormsModule]
})
export class ResetPasswordComponent implements OnInit {
  token = '';
  password = '';
  confirmPassword = '';
  message = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
  }

  submit() {
    if (!this.password || !this.confirmPassword) {
      this.message = 'Veuillez remplir tous les champs.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.message = 'Les mots de passe ne correspondent pas.';
      return;
    }

    const url = 'http://localhost:8080/userservice/auth/reset-password';
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    this.http.post(url, {
      token: this.token,
      newPassword: this.password
    }, { headers }).subscribe({
      next: () => {
        this.message = 'Mot de passe réinitialisé avec succès ! Redirection...';
        setTimeout(() => this.router.navigate(['/sign-in']), 2000);
      },
      error: (err) => {
        console.error('Reset password error', err);
        this.message = 'Erreur lors de la réinitialisation du mot de passe.';
      }
    });
  }
}
