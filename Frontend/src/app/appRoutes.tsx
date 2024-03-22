import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Expenditure } from '../pages/expenditure';
import { Finance } from '../pages/finance';
import { Landing } from '../pages/landing';
import { Main } from '../pages/main';
import { Market } from '../pages/market';
import { Mission } from '../pages/mission';
import { MyPage } from '../pages/myPage';
import { Ranking } from '../pages/ranking';
import { SignIn } from '../pages/signin';
import { SignUp } from '../pages/signup';
import { Team } from '../pages/team';
import { TeamInfo } from '../pages/teamInfo';
import { Test } from '../pages/test/ui/Test';
import { Navigate } from 'react-router-dom';

export function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/main" />} />
            <Route path="/main" element={<Main />} />
            <Route path="expenditure" element={<Expenditure />} />
            <Route path="finance" element={<Finance />} />
            <Route path="landing" element={<Landing />} />
            <Route path="market" element={<Market />} />
            <Route path="mission" element={<Mission />} />
            <Route path="my-page" element={<MyPage />} />
            <Route path="ranking" element={<Ranking />} />
            <Route path="signin" element={<SignIn />} />
            <Route path="signup" element={<SignUp />} />
            <Route path="team-info" element={<TeamInfo />} />
            <Route path="team" element={<Team />} />
            <Route path="test" element={<Test />} />
        </Routes>
    );
}
