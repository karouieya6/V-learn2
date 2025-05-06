import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Autoplay, EffectFade, Navigation, Pagination } from 'swiper/modules';
import Swiper from 'swiper';
import SwiperCore from 'swiper';

SwiperCore.use([Autoplay, EffectFade, Navigation, Pagination]);

@Component({
  selector: 'app-hero-slider',
  standalone: true,
  templateUrl: './hero-slider.component.html',
  styleUrls: ['./hero-slider.component.scss'],
  imports: [CommonModule, RouterModule]
})
export class HeroSliderComponent implements OnInit, AfterViewInit {

  @ViewChild('swiperContainer', { static: false }) swiperContainer!: ElementRef;

  sliderNavData = [
    {
      id: 1,
      img: 'assets/img/slider/nav/slider-nav-4.jpg',
      title: "6 Courses",
      subtitle: "Programming Languages",
      bgColor: "orange-bg",
    },
    {
      id: 2,
      img: 'assets/img/slider/nav/slider-nav-1.jpg',
      title: "4 Courses",
      subtitle: "Idea Discussion",
      bgColor: "blue-bg",
    },
    {
      id: 3,
      img: 'assets/img/slider/nav/slider-nav-2.jpg',
      title: "8 Courses",
      subtitle: "Web Development",
      bgColor: "pink-bg",
    },
    {
      id: 4,
      img: 'assets/img/slider/nav/slider-nav-3.jpg',
      title: "9 Courses",
      subtitle: "System Administration",
      bgColor: "green-bg",
    },
  ];

  constructor() {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    new Swiper(this.swiperContainer.nativeElement, {
      loop: true,
      effect: 'fade',
      autoplay: {
        delay: 4000,
        disableOnInteraction: false,
      },
      pagination: {
        el: '.hero-pagination',
        clickable: true,
      },
      navigation: {
        nextEl: '.hero-next',
        prevEl: '.hero-prev',
      },
      fadeEffect: {
        crossFade: true,
      },
    });
  }
} 