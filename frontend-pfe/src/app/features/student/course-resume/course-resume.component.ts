import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  NgbAccordionModule,
  NgbProgressbarModule,
  NgbCollapseModule,
  NgbModalModule,
  NgbModal,
} from '@ng-bootstrap/ng-bootstrap';
import aos from 'aos';

@Component({
  selector: 'app-course-resume',
  standalone: true,
  imports: [
    CommonModule,
    NgbAccordionModule,
    NgbProgressbarModule,
    NgbCollapseModule,
    NgbModalModule,
  ],
  templateUrl: './course-resume.component.html',
  styles: ``,
})
export class CourseResumeComponent implements OnInit {
  courses: any[] = []; // will receive from backend

  private modalService = inject(NgbModal);
  private http = inject(HttpClient); // ✅ inject HttpClient

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user.id;

    this.http.get<any[]>(`http://localhost:8080/enrollmentservice/api/enrollments/user/${userId}`)
      .subscribe({
        next: (data) => this.courses = data,
        error: () => console.error('Failed to load courses'),
      });

    aos.init();
  }

  openModal(content: TemplateRef<any>) {
    this.modalService.open(content, {
      modalDialogClass: 'modal-lg',
      centered: true,
    });
  }
}
