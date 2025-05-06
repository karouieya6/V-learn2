import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
@Component({
  standalone: true,
  selector: 'app-home-three-courses',
  templateUrl: './home-three-courses.component.html',
  styleUrls: ['./home-three-courses.component.scss'],
  imports: [
    CommonModule,
    RouterModule
  ]
})
export class HomeThreeCoursesComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
